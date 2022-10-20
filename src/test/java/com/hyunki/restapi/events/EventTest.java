package com.hyunki.restapi.events;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class EventTest {

    @Test
    public void builder() {
        Event event = Event.builder().build();
        Assertions.assertThat(event).isNotNull();
    }

    @Test
    public void javaBean()  {
        //given
        String name = "Event";
        String description = "Spring";

        //when
        Event event = new Event();
        event.setName(name);
        event.setDescription(description);

        System.out.println(event.hashCode());

        //then
        Assertions.assertThat(event.getName()).isEqualTo(name);
        Assertions.assertThat(event.getDescription()).isEqualTo(description);
    }
}