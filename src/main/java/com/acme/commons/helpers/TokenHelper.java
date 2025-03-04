package com.acme.commons.helpers;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;

import java.util.Map;

@Slf4j
public class TokenHelper {

    public static String prettyBody(String token) {

        try {
            log.info("Processing token: {}", token);

            String json = getPayload(token);

            ObjectMapper mapper = new ObjectMapper();

            // Use indentation of 4 spaces
            DefaultPrettyPrinter.Indenter indenter =
                    new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
            DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
            printer.indentObjectsWith(indenter);
            printer.indentArraysWith(indenter);

            // Format the JSON
            Map tokenBodyMap = mapper.readValue(json, Map.class);
            String prettyJson = mapper.writer(printer).writeValueAsString(tokenBodyMap);
            return prettyJson;
        } catch (Exception e) {

            log.error("It was an error to parse the token.", e);

            // If token is not a JWT
            return token;
        }
    }

    private static String getPayload(String token) {

        log.info("Payload to get from token: {}", token);

        // extract token body. Will throw exception if not a JWT
        String tokenBody = token.split("\\.")[1];
        String json = new String(Base64.decodeBase64URLSafe(tokenBody));

        log.info("Payload token gotten: {}", json);

        return json;
    }

}
