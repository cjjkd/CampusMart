package com.itcjj.campusmart.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class CurrentUser {
    private Long id;
    private String role;
}
