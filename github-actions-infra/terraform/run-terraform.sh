#!/bin/sh

tflocal init
tflocla plan
tflocal apply --auto-approve