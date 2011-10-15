#!/bin/sh

echo "== change to ffcop dev directory"
cd ~/ffcop/

echo "== build ffcop.war"
lein ring uberwar ffcop.war

echo "== move war to dev tomcat"
sudo cp ffcop.war /var/lib/tomcat6/webapps/
