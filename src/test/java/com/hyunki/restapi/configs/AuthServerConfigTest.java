package com.hyunki.restapi.configs;

import com.hyunki.restapi.accounts.Account;
import com.hyunki.restapi.accounts.AccountRole;
import com.hyunki.restapi.accounts.AccountService;
import com.hyunki.restapi.common.BaseControllerTest;
import com.hyunki.restapi.common.TestDescription;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @Test
    @TestDescription("인증 토큰을 발급 받는 테스트")
    public void getAuthToken() throws Exception {
        //Given
        String username = "hyunki@email.com";
        String password = "hyunki";

        Account hyunki = Account.builder()
                        .email(username)
                        .password(password)
                        .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                        .build();
        this.accountService.saveAccount(hyunki);

        String clientId = "myApp";
        String clientSecret = "pass";

        this.mockMvc.perform(post("/oauth/token")
                        .with(httpBasic(clientId, clientSecret))
                        .param("username", username)
                        .param("password", password)
                        .param("grant_type", "password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists());
    }
}