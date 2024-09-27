package skadi15;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AppServletTest {
    private static final String APPLES_PARAMETER = "apples";
    private static final String ORANGES_PARAMETER = "oranges";

    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private ServletOutputStream outputStreamMock;
    @Captor
    private ArgumentCaptor<String> outputCaptor;

    private AppServlet appServlet;

    @BeforeEach
    public void setup() throws IOException {
        appServlet = new AppServlet();
    }

    @Test
    @DisplayName("Order with both fruits")
    public void orderWithBothFruits() throws IOException {
        when(responseMock.getOutputStream()).thenReturn(outputStreamMock);

        final OrderSummary expectedResult = OrderSummary.builder()
                .numApples(1)
                .numOranges(2)
                .totalCost(1.45f)
                .build();

        appServlet.doPost(createRequest(expectedResult), responseMock);

        verify(responseMock).getOutputStream();

        verify(outputStreamMock).print(outputCaptor.capture());
        assertEquals(expectedResult.toString(), outputCaptor.getValue());

        verifyNoMoreInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Order with no items")
    public void orderWithNoItems() throws IOException {
        when(responseMock.getOutputStream()).thenReturn(outputStreamMock);

        final OrderSummary expectedResult = OrderSummary.builder()
                .numApples(0)
                .numOranges(0)
                .totalCost(0f)
                .build();

        appServlet.doPost(createRequest(expectedResult), responseMock);

        verify(responseMock).getOutputStream();

        verify(outputStreamMock).print(outputCaptor.capture());
        assertEquals(expectedResult.toString(), outputCaptor.getValue());

        verifyNoMoreInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Missing apples parameter")
    public void missingApplesParameter() {
        assertThrows(
            IllegalArgumentException.class,
            () -> appServlet.doPost(createRequest(null, "1"), responseMock)
        );

        verifyNoInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Missing oranges parameter")
    public void missingOrangesParameter() {
        assertThrows(
            IllegalArgumentException.class,
            () -> appServlet.doPost(createRequest("1", null), responseMock)
        );

        verifyNoInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Invalid apples value")
    public void invalidApplesValue() {
        assertThrows(
            IllegalArgumentException.class,
            () -> appServlet.doPost(createRequest("Bad value", "1"), responseMock)
        );

        verifyNoInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Invalid oranges value")
    public void invalidOrangesValue() {
        assertThrows(
            IllegalArgumentException.class,
            () -> appServlet.doPost(createRequest("1", "Bad value"), responseMock)
        );

        verifyNoInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Negative apples value")
    public void negativeApplesValue() {
        assertThrows(
            IllegalArgumentException.class,
            () -> appServlet.doPost(createRequest("-1", "1"), responseMock)
        );

        verifyNoInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Negative oranges value")
    public void negativeOrangesValue() {
        assertThrows(
            IllegalArgumentException.class,
            () -> appServlet.doPost(createRequest("1", "-1"), responseMock)
        );

        verifyNoInteractions(responseMock, outputStreamMock);
    }

    private HttpServletRequest createRequest(final OrderSummary expectedOrderSummary) {
        return createRequest(
            Integer.toString(expectedOrderSummary.getNumApples()),
            Integer.toString(expectedOrderSummary.getNumOranges())
        );
    }

    private HttpServletRequest createRequest(final String numApples, final String numOranges) {
        final Map<String, String[]> parameters = new HashMap<>();
        if (numApples != null) {
            parameters.put(APPLES_PARAMETER, new String[] {numApples.toString()});
        }
        if (numOranges != null) {
            parameters.put(ORANGES_PARAMETER, new String[] {numOranges.toString()});
        }
        return new TestHttpServletRequest(parameters);
    }
}
