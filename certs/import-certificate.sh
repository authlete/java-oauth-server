#!/bin/bash


KEYSTORE=$JAVA_HOME/jre/lib/security/cacerts
if [[ ! -f $KEYSTORE ]]; then
    KEYSTORE=$JAVA_HOME/lib/security/cacerts
fi

__import_certificate()
{
    local FILE="$1"
    local ALIAS=$(basename $FILE .pem)
    local COMMAND=(keytool -noprompt -storepass changeit -keystore $KEYSTORE -importcert -alias $ALIAS -file $FILE)

    echo "${COMMAND[@]}"
    "${COMMAND[@]}"
}


__main()
{
    if [ "$1" = "" ]; then
        echo "USAGE: $(basename $0) certificate.pem" 2>&1
        exit 1
    fi

    __import_certificate "$1"
}


__main "$@"
