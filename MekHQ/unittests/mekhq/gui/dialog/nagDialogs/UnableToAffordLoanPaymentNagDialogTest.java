/*
 * Copyright (c) 2024 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.gui.dialog.nagDialogs;

import mekhq.campaign.Campaign;
import mekhq.campaign.finances.Finances;
import mekhq.campaign.finances.Loan;
import mekhq.campaign.finances.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static mekhq.gui.dialog.nagDialogs.UnableToAffordLoanPaymentNagDialog.getTotalPaymentsDue;
import static mekhq.gui.dialog.nagDialogs.UnableToAffordLoanPaymentNagDialog.isUnableToAffordLoanPayment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class is a test class for the {@link UnableToAffordLoanPaymentNagDialog} class.
 * It contains tests for various scenarios related to the {@code getTotalPaymentsDue} and
 * {@code isUnableToAffordLoanPayment} methods
 */
class UnableToAffordLoanPaymentNagDialogTest {
    // Mock objects for the tests
    private Campaign campaign;
    private LocalDate today;
    private Finances finances;
    private Loan firstLoan, secondLoan;

    /**
     * Test setup for each test, runs before each test.
     * Initializes the mock objects and sets up the necessary mock behaviors.
     */
    @BeforeEach
    void init() {
        // Initialize the mock objects
        campaign = mock(Campaign.class);
        today = LocalDate.now();
        finances = mock(Finances.class);
        firstLoan = mock(Loan.class);
        secondLoan = mock(Loan.class);

        // Stubs
        when(campaign.getFinances()).thenReturn(finances);
        when(campaign.getLocalDate()).thenReturn(today);

        when(firstLoan.getPaymentAmount()).thenReturn(Money.of(5));
        when(secondLoan.getPaymentAmount()).thenReturn(Money.of(5));
    }

    /**
     * Initializes the loans with the specified number of days till the next payment.
     *
     * @param daysTillFirstLoan The number of days till the next payment for the first loan.
     * @param daysTillSecondLoan The number of days till the next payment for the second loan.
     */
    private void initializeLoans(int daysTillFirstLoan, int daysTillSecondLoan) {
        when(finances.getLoans()).thenReturn(List.of(firstLoan, secondLoan));

        when(firstLoan.getNextPayment()).thenReturn(today.plusDays(daysTillFirstLoan));
        when(secondLoan.getNextPayment()).thenReturn(today.plusDays(daysTillSecondLoan));
    }

    // In the following tests the getTotalPaymentsDue() method is called, and its response
    // is checked against expected behavior

    @Test
    void noLoans() {
        when(finances.getLoans()).thenReturn(new ArrayList<>());

        assertEquals(Money.zero(), getTotalPaymentsDue(campaign));
    }

    @Test
    void noLoanDueTomorrow() {
        initializeLoans(2, 2);

        assertEquals(Money.zero(), getTotalPaymentsDue(campaign));
    }

    @Test
    void oneLoanDueTomorrow() {
        initializeLoans(2, 1);

        assertEquals(Money.of(5), getTotalPaymentsDue(campaign));
    }

    @Test
    void twoLoansDueTomorrow() {
        initializeLoans(1, 1);

        assertEquals(Money.of(10), getTotalPaymentsDue(campaign));
    }

    // In the following tests the canAffordLoans() method is called, and its response is checked
    // against expected behavior

    @Test
    void canAffordLoans() {
        initializeLoans(2, 1);

        when(campaign.getFunds()).thenReturn(Money.of(10));

        assertFalse(isUnableToAffordLoanPayment(campaign));
    }

    @Test
    void cannotAffordLoans() {
        initializeLoans(1, 1);

        when(campaign.getFunds()).thenReturn(Money.of(5));

        assertTrue(isUnableToAffordLoanPayment(campaign));
    }
}