package org.restapi.springrestapi.service.post;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class LocalPostViewDebounceTest {

    LocalPostViewDebounce debounce = new LocalPostViewDebounce();

    @Test
    @DisplayName("동일 사용자, 게시글 조합에서 일정 시간 내 두 번째 호출은 true를 반환한다")
    void seenRecently_detectsRepeatedViews() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("X-Forwarded-For")).willReturn("127.0.0.1");
        given(request.getHeader("User-Agent")).willReturn("JUnit");

        // when
        boolean first = debounce.seenRecently(request, null, 9L);
        boolean second = debounce.seenRecently(request, null, 9L);

        // then
        assertThat(first).isFalse();
        assertThat(second).isTrue();
    }
}
