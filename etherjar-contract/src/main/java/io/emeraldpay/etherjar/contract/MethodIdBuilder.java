/*
 * Copyright (c) 2026 EmeraldPay Ltd, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.emeraldpay.etherjar.contract;

import io.emeraldpay.etherjar.abi.DefaultRepository;
import io.emeraldpay.etherjar.abi.Type;
import io.emeraldpay.etherjar.domain.MethodId;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Helper for building a MethodId from a function signature string, e.g. "transfer(address,uint256) returns (bool)".
 */
@NullMarked
public class MethodIdBuilder {

    /**
     * Creates a MethodId from a function name and parameter types.
     * @param name function name
     * @param types parameter types
     * @return MethodId
     */
    public static MethodId fromSignature(String name, Type... types) {
        return MethodIdBuilder.fromSignature(name, Arrays.asList(types));
    }

    /**
     * Creates a MethodId from a function name and parameter types.
     * @param name function name
     * @param types parameter types
     * @return MethodId
     */
    public static MethodId fromSignature(String name, List<Type> types) {
        return MethodId.fromSignature(name, types.stream().map(Type::getCanonicalName).toList());
    }

    /**
     * Parses a function signature string and creates a MethodId. The signature should be in the format:
     * "<code>functionName(type1, type2, ...) returns (returnType1, returnType2, ...)</code>", where the "returns" section is optional.
     *
     * @param signature function signature string
     * @return MethodId
     */
    public static MethodId parse(String signature) {
        Objects.requireNonNull(signature);

        String source = signature.trim();
        if (source.isEmpty()) {
            throw new IllegalArgumentException("Empty signature");
        }

        if (source.startsWith("function")) {
            source = source.substring("function".length()).trim();
        }

        int paramsStart = source.indexOf('(');
        if (paramsStart <= 0) {
            throw new IllegalArgumentException("Invalid signature: " + signature);
        }

        String name = source.substring(0, paramsStart).trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Invalid function name: " + signature);
        }

        int paramsEnd = findMatchingParen(source, paramsStart);
        String paramsPart = source.substring(paramsStart + 1, paramsEnd);

        Type.Repository repo = DefaultRepository.getInstance();
        List<String> inputTypes = parseTypeList(paramsPart, repo, "parameter", signature);

        // Parse return types as a validity check, though they do not affect MethodId.
        String tail = source.substring(paramsEnd + 1).trim();
        int returnsPos = tail.toLowerCase(Locale.ROOT).indexOf("returns");
        if (returnsPos >= 0) {
            String returnsSection = tail.substring(returnsPos + "returns".length()).trim();
            if (!returnsSection.startsWith("(")) {
                throw new IllegalArgumentException("Invalid returns section: " + signature);
            }
            int retEnd = findMatchingParen(returnsSection, 0);
            String retPart = returnsSection.substring(1, retEnd);
            parseTypeList(retPart, repo, "return", signature);
        }

        return MethodId.fromSignature(name, inputTypes);
    }

    private static List<String> parseTypeList(String section, Type.Repository repo, String elementKind, String signature) {
        String content = section.trim();
        if (content.isEmpty()) {
            return List.of();
        }

        String[] parts = content.split(",");
        List<String> result = new ArrayList<>(parts.length);

        for (String part : parts) {
            String token = part.trim();
            if (token.isEmpty()) {
                throw new IllegalArgumentException("Malformed " + elementKind + " list: " + signature);
            }
            String typeName = token.split("\\s+")[0];
            Type type = repo.search(typeName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown " + elementKind + " type: " + typeName));
            result.add(type.getCanonicalName());
        }

        return result;
    }

    private static int findMatchingParen(String source, int openPos) {
        int depth = 0;
        for (int i = openPos; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (ch == '(') {
                depth++;
            } else if (ch == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
                if (depth < 0) {
                    break;
                }
            }
        }
        throw new IllegalArgumentException("Unbalanced parentheses in signature: " + source);
    }
}
