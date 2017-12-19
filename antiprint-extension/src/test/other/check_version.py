#!/usr/bin/env python

from __future__ import print_function
import sys
import json
from argparse import ArgumentParser

if __name__ == '__main__':
    p = ArgumentParser()
    p.add_argument('required_version')
    p.add_argument('crx_manifest')
    args = p.parse_args()
    required_version = args.required_version
    if args.required_version.endswith('-SNAPSHOT'):
        required_version = required_version[:-len('-SNAPSHOT')]
    with open(args.crx_manifest, 'r') as ifile:
        manifest = json.load(ifile)
    manifest_version = manifest['version']
    if manifest_version != required_version:
        print("manifest version %s != pom version %s" % (required_version, manifest_version), file=sys.stderr)
        exit(2)
