package com.redhat.debezium.server.poc;

public class Main {
    public static void main(String...args) {
        OffsetFormatter formatter = new OffsetFormatter(args[0]);
        if (args.length < 2)
            // read offset data
            formatter.format();
        else {
            //TODO: implement write offset data
        } 
    }
}
