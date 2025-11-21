package com.innowise.rudkovskii.dto.order;

import com.innowise.rudkovskii.dto.user.UserInfoDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OrderWithUserDto {

    OrderDto order;
    UserInfoDto user;

}
