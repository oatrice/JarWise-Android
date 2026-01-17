package com.oatrice.jarwise.data.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SlipDetectionLogicTest {

    @Test
    fun `followsSlipQrPattern returns true for EMVCo standard`() {
        val promptPayPayload = "00020101021129370016A000000677010111011300668912345675802TH"
        assertTrue(SlipDetectionLogic.followsSlipQrPattern(promptPayPayload))
    }

    @Test
    fun `followsSlipQrPattern returns false for random text`() {
        assertFalse(SlipDetectionLogic.followsSlipQrPattern("Hello World"))
        assertFalse(SlipDetectionLogic.followsSlipQrPattern("https://google.com"))
    }

    @Test
    fun `containsSlipKeywords returns true when sufficient keywords present`() {
        val slipText = """
            โอนเงินสำเร็จ
            500.00 บาท
            Ref. 123456
        """.trimIndent()
        assertTrue(SlipDetectionLogic.containsSlipKeywords(slipText))
    }

    @Test
    fun `containsSlipKeywords returns false when insufficient keywords`() {
        val normalText = "This is a picture of a cat."
        assertFalse(SlipDetectionLogic.containsSlipKeywords(normalText))
    }
    
    @Test
    fun `containsSlipKeywords returns true for English slip`() {
        val slipText = "Transaction ID: 12345657 Amount: 50.00 Baht"
        assertTrue(SlipDetectionLogic.containsSlipKeywords(slipText))
    }
}
