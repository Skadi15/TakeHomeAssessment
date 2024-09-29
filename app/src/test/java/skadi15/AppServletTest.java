package skadi15;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AppServletTest {
    private static final String APPLES_PARAMETER = "apples";
    private static final String ORANGES_PARAMETER = "oranges";
    private static final String ORDER_ID_PARAMETER = "order_id";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
    @DisplayName("Order with both fruits and no deals")
    public void orderWithBothFruits() throws IOException {
        when(responseMock.getOutputStream()).thenReturn(outputStreamMock);

        final OrderSummary expectedResult = OrderSummary.builder()
                .numApples(1)
                .numOranges(2)
                .totalCost(1.45f)
                .build();

        appServlet.doPost(createPostRequest(expectedResult), responseMock);

        verify(responseMock).getOutputStream();

        verify(outputStreamMock).print(outputCaptor.capture());
        final OrderSummary actualOrder = parseOrderSummary(outputCaptor.getValue());
        assertTrue(expectedResult.isEquivalentTo(actualOrder));

        verifyNoMoreInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Order with both fruits and apple deal")
    public void orderWithAppleDeal() throws IOException {
        when(responseMock.getOutputStream()).thenReturn(outputStreamMock);

        final OrderSummary expectedResult = OrderSummary.builder()
                .numApples(5)
                .numOranges(2)
                .totalCost(1.95f)
                .build();

        appServlet.doPost(createPostRequest(expectedResult), responseMock);

        verify(responseMock).getOutputStream();

        verify(outputStreamMock).print(outputCaptor.capture());
        final OrderSummary actualOrder = parseOrderSummary(outputCaptor.getValue());
        assertTrue(expectedResult.isEquivalentTo(actualOrder));

        verifyNoMoreInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Order with both fruits and orange deal")
    public void orderWithOrangeDeal() throws IOException {
        when(responseMock.getOutputStream()).thenReturn(outputStreamMock);

        final OrderSummary expectedResult = OrderSummary.builder()
                .numApples(1)
                .numOranges(7)
                .totalCost(3.25f)
                .build();

        appServlet.doPost(createPostRequest(expectedResult), responseMock);

        verify(responseMock).getOutputStream();

        verify(outputStreamMock).print(outputCaptor.capture());
        final OrderSummary actualOrder = parseOrderSummary(outputCaptor.getValue());
        assertTrue(expectedResult.isEquivalentTo(actualOrder));

        verifyNoMoreInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Order with both fruits and both deals")
    public void orderWithBothDeals() throws IOException {
        when(responseMock.getOutputStream()).thenReturn(outputStreamMock);

        final OrderSummary expectedResult = OrderSummary.builder()
                .numApples(4)
                .numOranges(6)
                .totalCost(2.9f)
                .build();

        appServlet.doPost(createPostRequest(expectedResult), responseMock);

        verify(responseMock).getOutputStream();

        verify(outputStreamMock).print(outputCaptor.capture());
        final OrderSummary actualOrder = parseOrderSummary(outputCaptor.getValue());
        assertTrue(expectedResult.isEquivalentTo(actualOrder));

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

        appServlet.doPost(createPostRequest(expectedResult), responseMock);

        verify(responseMock).getOutputStream();

        verify(outputStreamMock).print(outputCaptor.capture());
        final OrderSummary actualOrder = parseOrderSummary(outputCaptor.getValue());
        assertTrue(expectedResult.isEquivalentTo(actualOrder));

        verifyNoMoreInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Order storage and retrieval")
    public void orderStorageAndRetrieval() throws IOException {
        when(responseMock.getOutputStream()).thenReturn(outputStreamMock);

        // Check stored orders before processing any.
        appServlet.doGet(createGetAllRequest(), responseMock);

        verify(responseMock).getOutputStream();

        verify(outputStreamMock).print("");

        clearInvocations(responseMock, outputStreamMock);

        // Store some orders.
        IntStream.range(0, 10).boxed()
                .map(i -> createPostRequest(i.toString(), i.toString()))
                .forEach(request -> appServlet.doPost(request, responseMock));
        
        verify(outputStreamMock, times(10)).print(outputCaptor.capture());
        final List<OrderSummary> orderSummaries = outputCaptor.getAllValues().stream()
                .map(this::parseOrderSummary)
                .toList();
        IntStream.range(0, 10).boxed().forEach(i -> 
            orderSummaries.stream()
                    .anyMatch(order -> order.getNumApples() == i && order.getNumOranges() == i)
        );

        clearInvocations(responseMock, outputStreamMock);

        // Check all stored orders.
        appServlet.doGet(createGetAllRequest(), responseMock);

        verify(outputStreamMock).print(outputCaptor.capture());
        final List<OrderSummary> storedOrderSummaries = OBJECT_MAPPER.readValue(
            "[" + outputCaptor.getValue() + "]",
            new TypeReference<List<OrderSummary>>() {}
        );
        
        orderSummaries.stream()
                .allMatch(order -> storedOrderSummaries.stream().anyMatch(order::equals));
        
        clearInvocations(responseMock, outputStreamMock);
        
        // Check a specific stored order.
        appServlet.doGet(createGetRequest(orderSummaries.get(0).getOrderId()), responseMock);

        verify(outputStreamMock).print(outputCaptor.capture());
        final OrderSummary retrievedOrder = parseOrderSummary(outputCaptor.getValue());
        assertEquals(orderSummaries.get(0), retrievedOrder);
    }

    @Test
    @DisplayName("Get an order that is not stored")
    public void getNotStoredOrder() throws IOException {
        when(responseMock.getOutputStream()).thenReturn(outputStreamMock);

        final UUID testUuid = UUID.randomUUID();

        appServlet.doGet(createGetRequest(testUuid), responseMock);

        verify(responseMock).getOutputStream();

        verify(outputStreamMock).print("No order found for ID " + testUuid.toString());

        verifyNoMoreInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Missing apples parameter")
    public void missingApplesParameter() {
        assertThrows(
            IllegalArgumentException.class,
            () -> appServlet.doPost(createPostRequest(null, "1"), responseMock)
        );

        verifyNoInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Missing oranges parameter")
    public void missingOrangesParameter() {
        assertThrows(
            IllegalArgumentException.class,
            () -> appServlet.doPost(createPostRequest("1", null), responseMock)
        );

        verifyNoInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Invalid apples value")
    public void invalidApplesValue() {
        assertThrows(
            IllegalArgumentException.class,
            () -> appServlet.doPost(createPostRequest("Bad value", "1"), responseMock)
        );

        verifyNoInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Invalid oranges value")
    public void invalidOrangesValue() {
        assertThrows(
            IllegalArgumentException.class,
            () -> appServlet.doPost(createPostRequest("1", "Bad value"), responseMock)
        );

        verifyNoInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Negative apples value")
    public void negativeApplesValue() {
        assertThrows(
            IllegalArgumentException.class,
            () -> appServlet.doPost(createPostRequest("-1", "1"), responseMock)
        );

        verifyNoInteractions(responseMock, outputStreamMock);
    }

    @Test
    @DisplayName("Negative oranges value")
    public void negativeOrangesValue() {
        assertThrows(
            IllegalArgumentException.class,
            () -> appServlet.doPost(createPostRequest("1", "-1"), responseMock)
        );

        verifyNoInteractions(responseMock, outputStreamMock);
    }

    private HttpServletRequest createPostRequest(final OrderSummary expectedOrderSummary) {
        return createPostRequest(
            Integer.toString(expectedOrderSummary.getNumApples()),
            Integer.toString(expectedOrderSummary.getNumOranges())
        );
    }

    private HttpServletRequest createPostRequest(final String numApples, final String numOranges) {
        final Map<String, String[]> parameters = new HashMap<>();
        if (numApples != null) {
            parameters.put(APPLES_PARAMETER, new String[] {numApples.toString()});
        }
        if (numOranges != null) {
            parameters.put(ORANGES_PARAMETER, new String[] {numOranges.toString()});
        }
        return new TestHttpServletRequest(parameters);
    }

    private HttpServletRequest createGetAllRequest() {
        return createGetRequest(null);
    }

    private HttpServletRequest createGetRequest(final UUID orderId) {
        final Map<String, String[]> parameters = new HashMap<>();
        if (orderId != null) {
            parameters.put(ORDER_ID_PARAMETER, new String[] {orderId.toString()});
        }
        return new TestHttpServletRequest(parameters);
    }

    private OrderSummary parseOrderSummary(final String seriaized) {
        try {
            return OBJECT_MAPPER.readValue(seriaized, OrderSummary.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
