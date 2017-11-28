package io.github.mike10004.extensibleffdriver;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandCodec;
import org.openqa.selenium.remote.HttpSessionId;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.ResponseCodec;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.http.W3CHttpCommandCodec;
import org.openqa.selenium.remote.http.W3CHttpResponseCodec;
import org.openqa.selenium.remote.service.DriverService;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Command executor that only handles addon install/uninstall commands and
 * requires the W3C dialect to be used.
 */
class LimitedCommandExecutor {

    private final Supplier<HttpClient> clientProvider;
    private CommandCodec<HttpRequest> commandCodec;
    private ResponseCodec<HttpResponse> responseCodec;

    public LimitedCommandExecutor(Supplier<? extends HttpClient> clientProvider) {
        this.clientProvider = Suppliers.memoize(clientProvider::get);
        commandCodec = new W3CHttpCommandCodec();
        responseCodec = new W3CHttpResponseCodec();
        Commands.getList().forEach(spec -> commandCodec.defineCommand(spec.name, spec.method, spec.url));
    }

    public static LimitedCommandExecutor forService(DriverService service, HttpClient.Factory httpClientFactory) {
        return new LimitedCommandExecutor(() -> httpClientFactory.createClient(service.getUrl()));
    }

    private static class CommandSpec {
        public final String name;
        public final HttpMethod method;
        public final String url;

        public CommandSpec(String name, HttpMethod method, String url) {
            this.name = Objects.requireNonNull(name);
            this.method = Objects.requireNonNull(method);
            this.url = Objects.requireNonNull(url);
        }
    }

    static final class Commands {

        private static final String URL_INSTALL_ADDON = "/session/:sessionId/moz/addon/install";
        private static final String URL_UNINSTALL_ADDON = "/session/:sessionId/moz/addon/uninstall";
        public static final String NAME_INSTALL_ADDON = "installAddon";
        public static final String NAME_UNINSTALL_ADDON = "uninstallAddon";
        private static final CommandSpec INSTALL_ADDON = new CommandSpec(Commands.NAME_INSTALL_ADDON, HttpMethod.POST, Commands.URL_INSTALL_ADDON);
        private static final CommandSpec UNINSTALL_ADDON = new CommandSpec(Commands.NAME_UNINSTALL_ADDON, HttpMethod.POST, Commands.URL_UNINSTALL_ADDON);
        private static final ImmutableList<CommandSpec> commands = ImmutableList.of(INSTALL_ADDON, UNINSTALL_ADDON);
        private static final ImmutableList<String> commandNames = commands.stream().map(cmd -> cmd.name).collect(ImmutableList.toImmutableList());

        private Commands() {}

        public static void checkSupportedCommand(String command) {
            Objects.requireNonNull(command, "command");
            if (!commandNames.contains(command)) {
                throw new IllegalUsageException("unsupported command");
            }
        }

        public static Iterable<CommandSpec> getList() {
            return commands;
        }
    }

    private static class IllegalUsageException extends RuntimeException {
        public IllegalUsageException(String message) {
            super(message);
        }
    }

    // Licensed to the Software Freedom Conservancy (SFC) under one
    // or more contributor license agreements.  See the NOTICE file
    // distributed with this work for additional information
    // regarding copyright ownership.  The SFC licenses this file
    // to you under the Apache License, Version 2.0 (the
    // "License"); you may not use this file except in compliance
    // with the License.  You may obtain a copy of the License at
    //
    //   http://www.apache.org/licenses/LICENSE-2.0
    //
    // Unless required by applicable law or agreed to in writing,
    // software distributed under the License is distributed on an
    // "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    // KIND, either express or implied.  See the License for the
    // specific language governing permissions and limitations
    // under the License.
    public Response execute(Command command) throws IOException {
        if (command.getSessionId() == null) {
            throw new IllegalUsageException("executor is only to be used with existing session");
        }
        Commands.checkSupportedCommand(command.getName());
        HttpRequest httpRequest = commandCodec.encode(command);
        try {
            HttpResponse httpResponse = clientProvider.get().execute(httpRequest, true);
            Response response = responseCodec.decode(httpResponse);
            if (response.getSessionId() == null) {
                if (httpResponse.getTargetHost() != null) {
                    response.setSessionId(HttpSessionId.getSessionId(httpResponse.getTargetHost()));
                } else {
                    // Spam in the session id from the request
                    response.setSessionId(command.getSessionId().toString());
                }
            }
            return response;
        } catch (UnsupportedCommandException e) {
            if (e.getMessage() == null || "".equals(e.getMessage())) {
                throw new UnsupportedOperationException(
                        "No information from server. Command name was: " + command.getName(),
                        e.getCause());
            }
            throw e;
        }
    }
}
