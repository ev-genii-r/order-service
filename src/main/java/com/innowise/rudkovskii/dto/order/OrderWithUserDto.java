package com.innowise.rudkovskii.dto.order;

import com.innowise.rudkovskii.dto.user.UserInfoDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class OrderWithUserDto {

   private OrderDto order;
   private UserInfoDto user;

}
