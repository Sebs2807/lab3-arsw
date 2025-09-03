/*
 * Copyright (c) 2025 Escuela Colombiana de Ingenieria Julio Garavito.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 *
 *     https://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 */

package co.eci.blacklist.api;

import co.eci.blacklist.api.dto.CheckResponseDTO;
import co.eci.blacklist.application.BlacklistService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/blacklist")
public class BlacklistController {

    private final BlacklistService service;

    // Expresión regular para validar direcciones IPv4
    private static final String IPV4_REGEX =
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";

    public BlacklistController(BlacklistService service) {
        this.service = service;
    }

    @GetMapping("/check")
    public ResponseEntity<CheckResponseDTO> check(
            @RequestParam
            @Pattern(regexp = IPV4_REGEX, message = "ip must be a valid IPv4 address")
            String ip,
            @RequestParam(defaultValue = "0")
            @Min(0) @Max(10_000) int threads) {

        int effectiveThreads = threads > 0
                ? threads
                : Math.max(1, Runtime.getRuntime().availableProcessors());

        var res = service.check(ip, effectiveThreads);
        return ResponseEntity.ok(CheckResponseDTO.from(res));
    }
}
