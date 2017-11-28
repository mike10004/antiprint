package io.github.mike10004.extensibleffdriver;

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
import org.openqa.selenium.remote.http.JsonHttpCommandCodec;
import org.openqa.selenium.remote.http.JsonHttpResponseCodec;
import org.openqa.selenium.remote.http.W3CHttpCommandCodec;
import org.openqa.selenium.remote.http.W3CHttpResponseCodec;
import org.openqa.selenium.remote.internal.ApacheHttpClient;
import org.openqa.selenium.remote.service.DriverService;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

class ExtendedCommandExecutor  {

    private final HttpClient client;
    private CommandCodec<HttpRequest> commandCodec;
    private ResponseCodec<HttpResponse> responseCodec;

    public ExtendedCommandExecutor(DriverService service) {
        this(service, new ApacheHttpClient.Factory());
    }

    public ExtendedCommandExecutor(DriverService service,
                                   HttpClient.Factory httpClientFactory) {
        URL url = service.getUrl();
        client = httpClientFactory.createClient(url);
        commandCodec = new W3CHttpCommandCodec();
        responseCodec = new W3CHttpResponseCodec();
        buildAdditionalCommands().forEach(this::defineCommand);
    }

    static class CommandSpec {
        public final String name;
        public final HttpMethod method;
        public final String url;

        public CommandSpec(String name, HttpMethod method, String url) {
            this.name = Objects.requireNonNull(name);
            this.method = Objects.requireNonNull(method);
            this.url = Objects.requireNonNull(url);
        }
    }

    /**
     * It may be useful to extend the commands understood by this {@code HttpCommandExecutor} at run
     * time, and this can be achieved via this method. Note, this is protected, and expected usage is
     * for subclasses only to call this.
     *
     * @param commandName The name of the command to use.
     * @param info CommandInfo for the command name provided
     */
    protected void defineCommand(CommandSpec spec) {
        commandCodec.defineCommand(spec.name, spec.method, spec.url);
    }

    public static Iterable<CommandSpec> buildAdditionalCommands() {
        return Arrays.asList(
                new CommandSpec(Commands.INSTALL_ADDON, HttpMethod.POST, "/session/:sessionId/moz/addon/install"),
                new CommandSpec(Commands.UNINSTALL_ADDON, HttpMethod.POST, "/session/:sessionId/moz/addon/uninstall")
        );
    }

    static final class Commands {

        public static final String INSTALL_ADDON = "installAddon";
        public static final String UNINSTALL_ADDON = "uninstallAddon";

        private Commands() {}

        public static void checkSupportedCommand(String command) {
            Objects.requireNonNull(command, "command");
            switch (command) {
                case INSTALL_ADDON:
                case UNINSTALL_ADDON:
                    return;
            }
            throw new IllegalUsageException("unsupported command");
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
            HttpResponse httpResponse = client.execute(httpRequest, true);
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
