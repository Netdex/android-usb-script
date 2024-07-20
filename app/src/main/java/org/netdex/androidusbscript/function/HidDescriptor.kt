package org.netdex.androidusbscript.function

import org.netdex.androidusbscript.configfs.function.HidParameters

/**
 * Descriptors for supported HID devices
 */
enum class HidDescriptor(val parameters: HidParameters) {
    KEYBOARD(
        HidParameters(
            protocol = 1,
            subclass = 1,
            reportLength = 8,
            byteArrayOf(
                0x05.toByte(), 0x01.toByte(),  /* USAGE_PAGE (GENERIC DESKTOP)	            */
                0x09.toByte(), 0x06.toByte(),  /* USAGE (KEYBOARD)                            */
                0xa1.toByte(), 0x01.toByte(),  /* COLLECTION (APPLICATION)                    */
                0x05.toByte(), 0x07.toByte(),  /*   USAGE_PAGE (KEYBOARD)                     */
                0x19.toByte(), 0xe0.toByte(),  /*   USAGE_MINIMUM (KEYBOARD LEFTCONTROL)      */
                0x29.toByte(), 0xe7.toByte(),  /*   USAGE_MAXIMUM (KEYBOARD RIGHT GUI)        */
                0x15.toByte(), 0x00.toByte(),  /*   LOGICAL_MINIMUM (0)                       */
                0x25.toByte(), 0x01.toByte(),  /*   LOGICAL_MAXIMUM (1)                       */
                0x75.toByte(), 0x01.toByte(),  /*   REPORT_SIZE (1)                           */
                0x95.toByte(), 0x08.toByte(),  /*   REPORT_COUNT (8)                          */
                0x81.toByte(), 0x02.toByte(),  /*   INPUT (DATA,VAR,ABS)                      */
                0x95.toByte(), 0x01.toByte(),  /*   REPORT_COUNT (1)                          */
                0x75.toByte(), 0x08.toByte(),  /*   REPORT_SIZE (8)                           */
                0x81.toByte(), 0x03.toByte(),  /*   INPUT (CNST,VAR,ABS)                      */
                0x95.toByte(), 0x05.toByte(),  /*   REPORT_COUNT (5)                          */
                0x75.toByte(), 0x01.toByte(),  /*   REPORT_SIZE (1)                           */
                0x05.toByte(), 0x08.toByte(),  /*   USAGE_PAGE (LEDS)                         */
                0x19.toByte(), 0x01.toByte(),  /*   USAGE_MINIMUM (NUM LOCK)                  */
                0x29.toByte(), 0x05.toByte(),  /*   USAGE_MAXIMUM (KANA)                      */
                0x91.toByte(), 0x02.toByte(),  /*   OUTPUT (DATA,VAR,ABS)                     */
                0x95.toByte(), 0x01.toByte(),  /*   REPORT_COUNT (1)                          */
                0x75.toByte(), 0x03.toByte(),  /*   REPORT_SIZE (3)                           */
                0x91.toByte(), 0x03.toByte(),  /*   OUTPUT (CNST,VAR,ABS)                     */
                0x95.toByte(), 0x06.toByte(),  /*   REPORT_COUNT (6)                          */
                0x75.toByte(), 0x08.toByte(),  /*   REPORT_SIZE (8)                           */
                0x15.toByte(), 0x00.toByte(),  /*   LOGICAL_MINIMUM (0)                       */
                0x25.toByte(), 0x65.toByte(),  /*   LOGICAL_MAXIMUM (101)                     */
                0x05.toByte(), 0x07.toByte(),  /*   USAGE_PAGE (KEYBOARD)                     */
                0x19.toByte(), 0x00.toByte(),  /*   USAGE_MINIMUM (RESERVED)                  */
                0x29.toByte(), 0x65.toByte(),  /*   USAGE_MAXIMUM (KEYBOARD APPLICATION)      */
                0x81.toByte(), 0x00.toByte(),  /*   INPUT (DATA,ARY,ABS)                      */
                0xc0.toByte()                  /* END_COLLECTION                              */
            )
        )
    ),
    MOUSE(
        HidParameters(
            protocol = 2,
            subclass = 1,
            reportLength = 4,
            byteArrayOf(
                0X05.toByte(), 0X01.toByte(),  /* USAGE PAGE (GENERIC DESKTOP CONTROLs)       */
                0X09.toByte(), 0X02.toByte(),  /* USAGE (MOUSE)                               */
                0XA1.toByte(), 0X01.toByte(),  /* COLLECTION (APPLICATION)                    */
                0X09.toByte(), 0X01.toByte(),  /*   USAGE (POINTER)                           */
                0XA1.toByte(), 0X00.toByte(),  /*   COLLECTION (PHYSICAL)                     */
                0X05.toByte(), 0X09.toByte(),  /*     USAGE PAGE (BUTTON)                     */
                0X19.toByte(), 0X01.toByte(),  /*     USAGE MINIMUM (1)                       */
                0X29.toByte(), 0X05.toByte(),  /*     USAGE MAXIMUM (5)                       */
                0X15.toByte(), 0X00.toByte(),  /*     LOGICAL MINIMUM (1)                     */
                0X25.toByte(), 0X01.toByte(),  /*     LOGICAL MAXIMUM (1)                     */
                0X95.toByte(), 0X05.toByte(),  /*     REPORT COUNT (5)                        */
                0X75.toByte(), 0X01.toByte(),  /*     REPORT SIZE (1)                         */
                0X81.toByte(), 0X02.toByte(),  /*     INPUT (DATA,VARIABLE,ABSOLUTE,BITFIELD) */
                0X95.toByte(), 0X01.toByte(),  /*     REPORT COUNT(1)                         */
                0X75.toByte(), 0X03.toByte(),  /*     REPORT SIZE(3)                          */
                0X81.toByte(), 0X01.toByte(),  /*     INPUT (CONSTANT,ARRAY,ABSOLUTE,BITFIELD)*/
                0X05.toByte(), 0X01.toByte(),  /*     USAGE PAGE (GENERIC DESKTOP CONTROLS)   */
                0X09.toByte(), 0X30.toByte(),  /*     USAGE (X)                               */
                0X09.toByte(), 0X31.toByte(),  /*     USAGE (Y)                               */
                0X09.toByte(), 0X38.toByte(),  /*     USAGE (WHEEL)                           */
                0X15.toByte(), 0X81.toByte(),  /*     LOGICAL MINIMUM (-127)                  */
                0X25.toByte(), 0X7F.toByte(),  /*     LOGICAL MAXIMUM (127)                   */
                0X75.toByte(), 0X08.toByte(),  /*     REPORT SIZE (8)                         */
                0X95.toByte(), 0X03.toByte(),  /*     REPORT COUNT (3)                        */
                0X81.toByte(), 0X06.toByte(),  /*     INPUT (DATA,VARIABLE,RELATIVE,BITFIELD) */
                0XC0.toByte(),                 /*   END COLLECTION                            */
                0XC0.toByte()                  /* END COLLECTION                              */
            )
        )
    )
}
