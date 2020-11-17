package org.netdex.hidfuzzer.function;

import org.netdex.hidfuzzer.configfs.function.UsbGadgetFunctionHid;

public enum HidDescriptor {
    KEYBOARD(new UsbGadgetFunctionHid.Parameters(
            1,
            1,
            8,
            new byte[]{
                    (byte) 0x05, (byte) 0x01,    /* USAGE_PAGE (GENERIC DESKTOP)	            */
                    (byte) 0x09, (byte) 0x06,    /* USAGE (KEYBOARD)                            */
                    (byte) 0xa1, (byte) 0x01,    /* COLLECTION (APPLICATION)                    */
                    (byte) 0x05, (byte) 0x07,    /*   USAGE_PAGE (KEYBOARD)                     */
                    (byte) 0x19, (byte) 0xe0,    /*   USAGE_MINIMUM (KEYBOARD LEFTCONTROL)      */
                    (byte) 0x29, (byte) 0xe7,    /*   USAGE_MAXIMUM (KEYBOARD RIGHT GUI)        */
                    (byte) 0x15, (byte) 0x00,    /*   LOGICAL_MINIMUM (0)                       */
                    (byte) 0x25, (byte) 0x01,    /*   LOGICAL_MAXIMUM (1)                       */
                    (byte) 0x75, (byte) 0x01,    /*   REPORT_SIZE (1)                           */
                    (byte) 0x95, (byte) 0x08,    /*   REPORT_COUNT (8)                          */
                    (byte) 0x81, (byte) 0x02,    /*   INPUT (DATA,VAR,ABS)                      */
                    (byte) 0x95, (byte) 0x01,    /*   REPORT_COUNT (1)                          */
                    (byte) 0x75, (byte) 0x08,    /*   REPORT_SIZE (8)                           */
                    (byte) 0x81, (byte) 0x03,    /*   INPUT (CNST,VAR,ABS)                      */
                    (byte) 0x95, (byte) 0x05,    /*   REPORT_COUNT (5)                          */
                    (byte) 0x75, (byte) 0x01,    /*   REPORT_SIZE (1)                           */
                    (byte) 0x05, (byte) 0x08,    /*   USAGE_PAGE (LEDS)                         */
                    (byte) 0x19, (byte) 0x01,    /*   USAGE_MINIMUM (NUM LOCK)                  */
                    (byte) 0x29, (byte) 0x05,    /*   USAGE_MAXIMUM (KANA)                      */
                    (byte) 0x91, (byte) 0x02,    /*   OUTPUT (DATA,VAR,ABS)                     */
                    (byte) 0x95, (byte) 0x01,    /*   REPORT_COUNT (1)                          */
                    (byte) 0x75, (byte) 0x03,    /*   REPORT_SIZE (3)                           */
                    (byte) 0x91, (byte) 0x03,    /*   OUTPUT (CNST,VAR,ABS)                     */
                    (byte) 0x95, (byte) 0x06,    /*   REPORT_COUNT (6)                          */
                    (byte) 0x75, (byte) 0x08,    /*   REPORT_SIZE (8)                           */
                    (byte) 0x15, (byte) 0x00,    /*   LOGICAL_MINIMUM (0)                       */
                    (byte) 0x25, (byte) 0x65,    /*   LOGICAL_MAXIMUM (101)                     */
                    (byte) 0x05, (byte) 0x07,    /*   USAGE_PAGE (KEYBOARD)                     */
                    (byte) 0x19, (byte) 0x00,    /*   USAGE_MINIMUM (RESERVED)                  */
                    (byte) 0x29, (byte) 0x65,    /*   USAGE_MAXIMUM (KEYBOARD APPLICATION)      */
                    (byte) 0x81, (byte) 0x00,    /*   INPUT (DATA,ARY,ABS)                      */
                    (byte) 0xc0                  /* END_COLLECTION                              */
            }
    )),
    MOUSE(new UsbGadgetFunctionHid.Parameters(
            2,
            1,
            4,
            new byte[]{
                    (byte) 0X05, (byte) 0X01,    /* USAGE PAGE (GENERIC DESKTOP CONTROLs)       */
                    (byte) 0X09, (byte) 0X02,    /* USAGE (MOUSE)                               */
                    (byte) 0XA1, (byte) 0X01,    /* COLLECTION (APPLICATION)                    */
                    (byte) 0X09, (byte) 0X01,    /*   USAGE (POINTER)                           */
                    (byte) 0XA1, (byte) 0X00,    /*   COLLECTION (PHYSICAL)                     */
                    (byte) 0X05, (byte) 0X09,    /*     USAGE PAGE (BUTTON)                     */
                    (byte) 0X19, (byte) 0X01,    /*     USAGE MINIMUM (1)                       */
                    (byte) 0X29, (byte) 0X05,    /*     USAGE MAXIMUM (5)                       */
                    (byte) 0X15, (byte) 0X00,    /*     LOGICAL MINIMUM (1)                     */
                    (byte) 0X25, (byte) 0X01,    /*     LOGICAL MAXIMUM (1)                     */
                    (byte) 0X95, (byte) 0X05,    /*     REPORT COUNT (5)                        */
                    (byte) 0X75, (byte) 0X01,    /*     REPORT SIZE (1)                         */
                    (byte) 0X81, (byte) 0X02,    /*     INPUT (DATA,VARIABLE,ABSOLUTE,BITFIELD) */
                    (byte) 0X95, (byte) 0X01,    /*     REPORT COUNT(1)                         */
                    (byte) 0X75, (byte) 0X03,    /*     REPORT SIZE(3)                          */
                    (byte) 0X81, (byte) 0X01,    /*     INPUT (CONSTANT,ARRAY,ABSOLUTE,BITFIELD)*/
                    (byte) 0X05, (byte) 0X01,    /*     USAGE PAGE (GENERIC DESKTOP CONTROLS)   */
                    (byte) 0X09, (byte) 0X30,    /*     USAGE (X)                               */
                    (byte) 0X09, (byte) 0X31,    /*     USAGE (Y)                               */
                    (byte) 0X09, (byte) 0X38,    /*     USAGE (WHEEL)                           */
                    (byte) 0X15, (byte) 0X81,    /*     LOGICAL MINIMUM (-127)                  */
                    (byte) 0X25, (byte) 0X7F,    /*     LOGICAL MAXIMUM (127)                   */
                    (byte) 0X75, (byte) 0X08,    /*     REPORT SIZE (8)                         */
                    (byte) 0X95, (byte) 0X03,    /*     REPORT COUNT (3)                        */
                    (byte) 0X81, (byte) 0X06,    /*     INPUT (DATA,VARIABLE,RELATIVE,BITFIELD) */
                    (byte) 0XC0,                 /*   END COLLECTION                            */
                    (byte) 0XC0                  /* END COLLECTION                              */

            }));

    private final UsbGadgetFunctionHid.Parameters parameters_;

    HidDescriptor(UsbGadgetFunctionHid.Parameters parameters) {
        this.parameters_ = parameters;
    }

    public UsbGadgetFunctionHid.Parameters getParameters() {
        return parameters_;
    }
}
