package com.hyunki.restapi.events;

import com.hyunki.restapi.accounts.Account;
import com.hyunki.restapi.accounts.AccountRepository;
import com.hyunki.restapi.accounts.AccountRole;
import com.hyunki.restapi.accounts.AccountService;
import com.hyunki.restapi.common.AppProperties;
import com.hyunki.restapi.common.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerTests extends BaseTest {
    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AppProperties appProperties;

    @BeforeEach
    public void setUp() {
        this.eventRepository.deleteAll();;
        this.accountRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23,14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 23))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();

        mockMvc.perform(post("/api/events/")
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query events"),
                                linkWithRel("update-event").description("link to update an existing event"),
                                linkWithRel("profile").description("link to update an existing event")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base price of new event"),
                                fieldWithPath("maxPrice").description("max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit or Enrollment")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                        ),
                        responseFields(
                                fieldWithPath("id").description("identifier of new event"),
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base price of new event"),
                                fieldWithPath("maxPrice").description("max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit or Enrollment"),
                                fieldWithPath("free").description("it tells if this event is free or not"),
                                fieldWithPath("offline").description("it tells if this event is offline event or not"),
                                fieldWithPath("eventStatus").description("event status"),
                                fieldWithPath("manager").description("event manager"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.query-events.href").description("link to query event list"),
                                fieldWithPath("_links.update-event.href").description("link to update existing event"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("manager.id").description("identifier of event manager")
                        )
                ));
        ;
    }

    private String getBearerToken() throws Exception {
        return getBearerToken(true);
    }

    private String getBearerToken(boolean needToCreateAccount) throws Exception {
        return "Bearer " + getAccessToken(needToCreateAccount);
    }

    private String getAccessToken(boolean needToCreateAccount) throws Exception {
        //Given
        if(needToCreateAccount) {
            createAccount();
        }

        ResultActions perform = this.mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("username", appProperties.getUserUserName())
                .param("password", appProperties.getUserPassword())
                .param("grant_type", "password"));
        var responseBody = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();

        return parser.parseMap(responseBody).get("access_token").toString();
    }

    private Account createAccount() {
        Account account = Account.builder()
                .email(appProperties.getUserUserName())
                .password(appProperties.getUserPassword())
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        return this.accountService.saveAccount(account);
    }

    @Test
    @DisplayName("입력 받을 수 없는 값을 사용한 경우에는 에러가 발생하는 테스트")
    public void createEvent_Bad_Request() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23,14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 23))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();

        mockMvc.perform(post("/api/events/")
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken() )
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("입력 값이 비어있는 경우에는 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        this.mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("입력 값이 잘못된 경우에는 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23,14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 23))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();

        this.mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].objectName").exists())
                .andExpect(jsonPath("errors[0].defaultMessage").exists())
                .andExpect(jsonPath("errors[0].code").exists())
                .andExpect(jsonPath("_links.index").exists())
        ;
    }

    @Test
    @DisplayName("30개의 이벤트를 10개씩, 두번째 페이지 조회하기")
    public void queryEvents() throws Exception {
        //given
        IntStream.range(0, 30).forEach(this::generateEvent);

        //when
        this.mockMvc.perform(get("/api/events")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "id,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-events",
                        requestParameters(
                                parameterWithName("page").description("요청할 페이지, 페이지는 0부터 시작합니다."),
                                parameterWithName("size").description("요청할 데이터 개수"),
                                parameterWithName("sort").description("요청할 데이터의 정렬 기준, 정렬 순서")),
                        responseHeaders(
                                headerWithName("Content-type").description("content type header")),
                        responseFields(
                                fieldWithPath("_embedded.eventList[].id").description("identifier of event"),
                                fieldWithPath("_embedded.eventList[].name").description("name of event"),
                                fieldWithPath("_embedded.eventList[].description").description("description of event"),
                                fieldWithPath("_embedded.eventList[].beginEnrollmentDateTime").description("date time of begin enrollment of event"),
                                fieldWithPath("_embedded.eventList[].closeEnrollmentDateTime").description("date time of close enrollment of event"),
                                fieldWithPath("_embedded.eventList[].beginEventDateTime").description("date time of begin event"),
                                fieldWithPath("_embedded.eventList[].endEventDateTime").description("date time of end event"),
                                fieldWithPath("_embedded.eventList[].location").description("location of event"),
                                fieldWithPath("_embedded.eventList[].basePrice").description("base price of event"),
                                fieldWithPath("_embedded.eventList[].maxPrice").description("max price of event"),
                                fieldWithPath("_embedded.eventList[].limitOfEnrollment").description("limit or enrollment"),
                                fieldWithPath("_embedded.eventList[].free").description("it tells this event is free or not"),
                                fieldWithPath("_embedded.eventList[].offline").description("it tells this event is offline or not"),
                                fieldWithPath("_embedded.eventList[].eventStatus").description("event status"),
                                fieldWithPath("_embedded.eventList[].manager").description("event manager"),
                                fieldWithPath("_embedded.eventList[]._links.self.href").description("이 이벤트의 상세 페이지 링크 정보"),
                                fieldWithPath("_links.first.href").description("첫 번째 페이지로 이동할 링크"),
                                fieldWithPath("_links.prev.href").description("이전 페이지로 이동할 링크"),
                                fieldWithPath("_links.self.href").description("현재 페이지로 이동할 링크"),
                                fieldWithPath("_links.next.href").description("다음 페이지로 이동할 링크"),
                                fieldWithPath("_links.last.href").description("마지막 페이지로 이동할 링크"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("page.size").description("응답된 데이터 개수"),
                                fieldWithPath("page.totalElements").description("전체 데이터 개수"),
                                fieldWithPath("page.totalPages").description("전체 페이지 개수"),
                                fieldWithPath("page.number").description("응답된 목록의 페이지 번호")
                        ),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("next").description("다음 페이지로 이동할 링크"),
                                linkWithRel("last").description("마지막 페이지로 이동할 링크"),
                                linkWithRel("prev").description("이전 페이지로 이동할 링크"),
                                linkWithRel("first").description("첫 번째 페이지로 이동할 링크"),
                                linkWithRel("profile").description("link to profile")
                        )

                ));
    }

    @Test
    @DisplayName("30개의 이벤트를 10개씩, 두번째 페이지 조회하기")
    public void queryEventsWithAuthentication() throws Exception {
        //given
        IntStream.range(0, 30).forEach(this::generateEvent);

        //when
        this.mockMvc.perform(get("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "id,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.create-event").exists())
                .andDo(document("query-events",
                        requestParameters(
                                parameterWithName("page").description("요청할 페이지, 페이지는 0부터 시작합니다."),
                                parameterWithName("size").description("요청할 데이터 개수"),
                                parameterWithName("sort").description("요청할 데이터의 정렬 기준, 정렬 순서")),
                        responseHeaders(
                                headerWithName("Content-type").description("content type header")),
                        responseFields(
                                fieldWithPath("_embedded.eventList[].id").description("identifier of event"),
                                fieldWithPath("_embedded.eventList[].name").description("name of event"),
                                fieldWithPath("_embedded.eventList[].description").description("description of event"),
                                fieldWithPath("_embedded.eventList[].beginEnrollmentDateTime").description("date time of begin enrollment of event"),
                                fieldWithPath("_embedded.eventList[].closeEnrollmentDateTime").description("date time of close enrollment of event"),
                                fieldWithPath("_embedded.eventList[].beginEventDateTime").description("date time of begin event"),
                                fieldWithPath("_embedded.eventList[].endEventDateTime").description("date time of end event"),
                                fieldWithPath("_embedded.eventList[].location").description("location of event"),
                                fieldWithPath("_embedded.eventList[].basePrice").description("base price of event"),
                                fieldWithPath("_embedded.eventList[].maxPrice").description("max price of event"),
                                fieldWithPath("_embedded.eventList[].limitOfEnrollment").description("limit or enrollment"),
                                fieldWithPath("_embedded.eventList[].free").description("it tells this event is free or not"),
                                fieldWithPath("_embedded.eventList[].offline").description("it tells this event is offline or not"),
                                fieldWithPath("_embedded.eventList[].eventStatus").description("event status"),
                                fieldWithPath("_embedded.eventList[].manager").description("event manager"),
                                fieldWithPath("_embedded.eventList[]._links.self.href").description("이 이벤트의 상세 페이지 링크 정보"),
                                fieldWithPath("_links.first.href").description("첫 번째 페이지로 이동할 링크"),
                                fieldWithPath("_links.prev.href").description("이전 페이지로 이동할 링크"),
                                fieldWithPath("_links.self.href").description("현재 페이지로 이동할 링크"),
                                fieldWithPath("_links.next.href").description("다음 페이지로 이동할 링크"),
                                fieldWithPath("_links.last.href").description("마지막 페이지로 이동할 링크"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.create-event.href").description("link to create event"),
                                fieldWithPath("page.size").description("응답된 데이터 개수"),
                                fieldWithPath("page.totalElements").description("전체 데이터 개수"),
                                fieldWithPath("page.totalPages").description("전체 페이지 개수"),
                                fieldWithPath("page.number").description("응답된 목록의 페이지 번호")
                        ),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("next").description("다음 페이지로 이동할 링크"),
                                linkWithRel("last").description("마지막 페이지로 이동할 링크"),
                                linkWithRel("prev").description("이전 페이지로 이동할 링크"),
                                linkWithRel("first").description("첫 번째 페이지로 이동할 링크"),
                                linkWithRel("profile").description("link to profile"),
                                linkWithRel("create-event").description("link to create event")
                        )

                ));
    }

    @Test
    @DisplayName("기존의 이벤트를 하나 조회하기")
    public void getEvent() throws Exception {
        //Given
        Account account = this.createAccount();
        Event event = this.generateEvent(100, account);

        // When & Then
        this.mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-an-event",
                        responseHeaders(headerWithName("Content-Type").description("Content type header")),
                        responseFields(
                                fieldWithPath("id").description("identifier of event"),
                                fieldWithPath("name").description("Name of event"),
                                fieldWithPath("description").description("description of event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of event"),
                                fieldWithPath("endEventDateTime").description("date time of end of event"),
                                fieldWithPath("location").description("location of event"),
                                fieldWithPath("basePrice").description("base price of event"),
                                fieldWithPath("maxPrice").description("max price of event"),
                                fieldWithPath("limitOfEnrollment").description("limit or Enrollment"),
                                fieldWithPath("free").description("it tells if this event is free or not"),
                                fieldWithPath("offline").description("it tells if this event is offline event or not"),
                                fieldWithPath("eventStatus").description("event status"),
                                fieldWithPath("manager").description("event manager"),
                                fieldWithPath("manager.id").description("identifier of event manager"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile")
                        ),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("profile").description("link to update an existing event")
                        )));
    }

    @Test
    @DisplayName("없는 이벤트를 조회 했을때 404 응답받기")
    public void getEvent404() throws Exception {
        this.mockMvc.perform(get("/api/events/{id}", "11883"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("이벤트를 정상적으로 수정하기")
    public void updateEvent() throws Exception {
        //given
        Account account = this.createAccount();
        Event event = this.generateEvent(200, account);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        String eventName = "Updated Event";
        eventDto.setName(eventName);

        //when & then
        //accept header 해도 안해도 상관없음
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken(false))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(eventName))
                .andExpect(jsonPath("_links.self").exists())
                .andDo(document("update-event",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of event"),
                                fieldWithPath("description").description("description of event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of event"),
                                fieldWithPath("endEventDateTime").description("date time of end of event"),
                                fieldWithPath("location").description("location of event"),
                                fieldWithPath("basePrice").description("base price of event"),
                                fieldWithPath("maxPrice").description("max price of event"),
                                fieldWithPath("limitOfEnrollment").description("limit or Enrollment")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("identifier of updated event"),
                                fieldWithPath("name").description("Name of updated event"),
                                fieldWithPath("description").description("description of updated event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of event"),
                                fieldWithPath("endEventDateTime").description("date time of end of event"),
                                fieldWithPath("location").description("location of event"),
                                fieldWithPath("basePrice").description("base price of event"),
                                fieldWithPath("maxPrice").description("max price of event"),
                                fieldWithPath("limitOfEnrollment").description("limit or Enrollment"),
                                fieldWithPath("free").description("it tells if this event is free or not"),
                                fieldWithPath("offline").description("it tells if this event is offline event or not"),
                                fieldWithPath("eventStatus").description("event status"),
                                fieldWithPath("manager").description("event manager"),
                                fieldWithPath("manager.id").description("identifier of event manager"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile")
                        ),
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("profile").description("link to update an existing event")
                        )));
    }

    @Test
    @DisplayName("입력값이 비어있는 경우에 이벤트 수정 실패")
    public void updateEvent400_Empty() throws Exception {
        //Given
        Event event = this.generateEvent(200);
        EventDto eventDto = new EventDto();

        //When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("입력값이 잘못된 경우에 이벤트 수정 실패")
    public void updateEvent400_Wrong() throws Exception {
        //given
        Event event = this.generateEvent(200);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        //when & then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 이벤트 수정 실패")
    public void updateEvent404() throws Exception {
        // Given
        Event event = this.generateEvent(200);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);

        this.mockMvc.perform(put("/api/events/12351341")
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                        .andDo(print())
                        .andExpect(status().isNotFound());
    }

    private Event generateEvent(int index, Account account) {
        Event event = buildEvent(index);
        event.setManager(account);
        return this.eventRepository.save(event);
    }
    private Event generateEvent(int index) {
        Event event = buildEvent(index);
        return this.eventRepository.save(event);
    }

    private Event buildEvent(int index) {
        Event event = Event.builder()
                .name("event " + index)
                .description("test event")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23,14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 23))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .build();

        return event;
    }

}
