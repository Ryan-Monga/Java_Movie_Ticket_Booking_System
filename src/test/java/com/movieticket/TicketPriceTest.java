package com.movieticket;

import com.movieticket.model.IMAXTicket;
import com.movieticket.model.PremiumTicket;
import com.movieticket.model.RegularTicket;
import com.movieticket.model.Ticket;
import com.movieticket.model.TicketCategory;
import com.movieticket.util.PriceCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests verifying polymorphism and price calculation formulas.
 * Satisfies Requirement 13.1.
 */
public class TicketPriceTest {

    @Test
    @DisplayName("1. Regular Ticket calculates exact base price")
    public void testRegularTicketPrice() {
        double basePrice = 300.0;
        Ticket ticket = new RegularTicket("BK101", "Inception", "IMAX City", "07:30 PM", "C4", basePrice);
        assertEquals(300.0, ticket.calculatePrice(), 0.001);
        assertEquals("Regular", ticket.getCategoryName());
    }

    @Test
    @DisplayName("2. Premium Ticket calculates 1.5x base price")
    public void testPremiumTicketPrice() {
        double basePrice = 300.0;
        Ticket ticket = new PremiumTicket("BK101", "Inception", "IMAX City", "07:30 PM", "C4", basePrice);
        assertEquals(450.0, ticket.calculatePrice(), 0.001);
        assertEquals("Premium", ticket.getCategoryName());
    }

    @Test
    @DisplayName("3. IMAX Ticket calculates base price + 250 premium")
    public void testIMAXTicketPrice() {
        double basePrice = 300.0;
        Ticket ticket = new IMAXTicket("BK101", "Inception", "IMAX City", "07:30 PM", "C4", basePrice);
        assertEquals(550.0, ticket.calculatePrice(), 0.001);
        assertEquals("IMAX 3D", ticket.getCategoryName());
    }

    @Test
    @DisplayName("4. Two Regular seats total exactly Rs 600 in demonstration scenario")
    public void testTwoSeatBookingDemonstrationScenario() {
        double basePrice = 300.0;
        int seatCount = 2; // C4, C5
        double total = PriceCalculator.calculateTotal(TicketCategory.REGULAR, basePrice, seatCount);
        assertEquals(600.0, total, 0.001, "Two seats for Inception regular ticket must total Rs 600");
    }

    @Test
    @DisplayName("5. Polymorphic Factory creates correct Ticket subclasses")
    public void testPolymorphicFactory() {
        Ticket t1 = PriceCalculator.createTicket(TicketCategory.REGULAR, "BK1", "M", "T", "07:30", "A1", 300.0);
        Ticket t2 = PriceCalculator.createTicket(TicketCategory.PREMIUM, "BK1", "M", "T", "07:30", "A2", 300.0);
        Ticket t3 = PriceCalculator.createTicket(TicketCategory.IMAX, "BK1", "M", "T", "07:30", "A3", 300.0);

        assertNotNull(t1);
        assertEquals(RegularTicket.class, t1.getClass());
        assertEquals(PremiumTicket.class, t2.getClass());
        assertEquals(IMAXTicket.class, t3.getClass());
    }
}
