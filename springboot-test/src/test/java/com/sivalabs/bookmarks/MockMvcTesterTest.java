package com.sivalabs.bookmarks;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class MockMvcTesterTest {

  @Autowired
  private MockMvcTester mvc;

  @Test
  void shouldGetProductList() {
    var result = mvc.get()
                    .uri("/api/bookmarks");
    Assertions.assertThat(result)
              .hasStatusOk()
              .bodyJson()
              .convertTo(List.class)
              .satisfies(list -> {
                Assertions.assertThat(list.size()).isEqualTo(5);
              });
  }

  @Test
  void shouldGetProductByCode() {
    var result = mvc.get()
                    .uri("/api/bookmarks/{id}", "1");
    Assertions.assertThat(result)
              .hasStatusOk()
              .bodyJson()
              .hasPathSatisfying("$.id",
                  id -> Assertions.assertThat(id).isEqualTo(1));
  }

}
