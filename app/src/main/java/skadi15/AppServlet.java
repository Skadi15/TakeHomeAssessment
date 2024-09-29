package skadi15;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/order")
public class AppServlet extends HttpServlet {
    private static final String APPLES_PARAMETER = "apples";
    private static final String ORANGES_PARAMETER = "oranges";
    private static final String ORDER_ID_PARAMETER = "order_id";
    private static final float APPLE_COST = 0.25f;
    private static final float ORANGE_COST = 0.6f;

    private Map<UUID, OrderSummary> orders = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doPost(final HttpServletRequest req, final HttpServletResponse resp) {
        final int numApples = checkIntParameter(req, APPLES_PARAMETER);
        final int numOranges = checkIntParameter(req, ORANGES_PARAMETER);

        try {
            resp.getOutputStream().print(objectMapper.writeValueAsString(processOrder(numApples, numOranges)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) {
        try {
            final UUID orderId = checkUuidParameter(req, ORDER_ID_PARAMETER);
            
            // If no orderId given, return all orders.
            if (orderId == null) {
                resp.getOutputStream().print(
                    getOrders().stream()
                            .map(order -> {
                                try {
                                    return objectMapper.writeValueAsString(order);
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .collect(Collectors.joining(","))
                );
            } else {
                final OrderSummary order = getOrder(orderId);
                if (order == null) {
                    resp.getOutputStream().print("No order found for ID " + orderId.toString());
                } else {
                    resp.getOutputStream().print(objectMapper.writeValueAsString(order));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private OrderSummary processOrder(final int numApples, final int numOranges) {
        final float appleCost = Math.ceilDiv(numApples, 2) * APPLE_COST; // BOGO free
        final float orangeCost = (numOranges / 3 * 2 + numOranges % 3) * ORANGE_COST; // 3 for the price of 2
        final OrderSummary order = OrderSummary.builder()
                .orderId(UUID.randomUUID())
                .numApples(numApples)
                .numOranges(numOranges)
                .totalCost(appleCost + orangeCost)
                .build();
        recordOrder(order);
        return order;
    }

    public void recordOrder(final OrderSummary order) {
        orders.put(order.getOrderId(), order);
    }

    public OrderSummary getOrder(final UUID orderId) {
        return orders.get(orderId);
    }

    public Collection<OrderSummary> getOrders() {
        return orders.values();
    }

    private int checkIntParameter(final HttpServletRequest request, final String parameter) {
        Preconditions.checkArgument(request.getParameterMap().containsKey(parameter), String.format("Parameter %s not in request", parameter));

        final String rawValue = request.getParameter(parameter);
        try {
            final int value = Integer.parseInt(rawValue);
            Preconditions.checkArgument(value >= 0, String.format("Parameter %s must not be negative [value=%d]", parameter, value));
            return value;
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Parameter %s does not contain a valid integer [value=%s]", parameter, rawValue));
        }
    }

    private UUID checkUuidParameter(final HttpServletRequest request, final String parameter) {
        if (!request.getParameterMap().containsKey(parameter)) {
            return null;
        }

        final String rawValue = request.getParameter(parameter);
        return UUID.fromString(rawValue);
    }
}
