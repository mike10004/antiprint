#!/usr/bin/env python3

from __future__ import print_function
import sys
import json
from argparse import ArgumentParser

ERR_VERSION_MISMATCH = 2


def check_efw_pom(pom_file, required_version):
    # Version is interpolated from ${project.parent.version}, so this check is no longer necessary
    pass


def check_manifest(crx_manifest, required_version):
    if required_version.endswith('-SNAPSHOT'):
        required_version = required_version[:-len('-SNAPSHOT')]
    with open(crx_manifest, 'r') as ifile:
        manifest = json.load(ifile)
    manifest_version = manifest['version']
    if manifest_version != required_version:
        print("manifest version (from %s) %s != required version %s (derived from pom.xml)" % (crx_manifest, manifest_version, required_version), file=sys.stderr)
        exit(ERR_VERSION_MISMATCH)


if __name__ == '__main__':
    p = ArgumentParser()
    p.add_argument('required_version')
    p.add_argument('crx_manifest')
    p.add_argument('efw_pom_file')
    args = p.parse_args()
    check_manifest(args.crx_manifest, args.required_version)
    check_efw_pom(args.efw_pom_file, args.required_version)
