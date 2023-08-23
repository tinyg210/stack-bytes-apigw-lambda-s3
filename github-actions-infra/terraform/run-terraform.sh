#!/bin/sh

tflocal init
tflocal plan
tflocal apply --auto-approve