package com.hyunki.restapi.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@SpringBootTest //웹관련 테스트는 이걸로하는게 좋다, 왜냐하면 mock할게 너무많아서 힘들다(테스트코드작성이 힘들다)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
@ActiveProfiles("test")
@Ignore //테스트를 진행하는 클래스로 간주되지 않도록
public class BaseControllerTest {

    //dispatcherServlet에 가짜 요청과 응답을 확인할수있도록
    //웹서버를 띄우지않는다, 단위테스트보다는 좀 무거움
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    protected ObjectMapper objectMapper;
}
