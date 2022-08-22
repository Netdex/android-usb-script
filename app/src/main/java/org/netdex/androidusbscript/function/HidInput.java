package org.netdex.androidusbscript.function;

/**
 * Created by netdex on 1/15/2017.
 */

public class HidInput {

    public static class Mouse {
        public enum Button {
            BTN_NONE(0x0),
            BTN_LEFT(0x1),
            BTN_RIGHT(0x2),
            BTN_MIDDLE(0x4);
            public final byte code;

            Button(int code) {
                this.code = (byte) code;
            }
        }
    }

    public static class Keyboard {
        public enum Mod {
            MOD_NONE(0x0),
            MOD_LCTRL(0x1),
            MOD_LSHIFT(0x2),
            MOD_LALT(0x4),
            MOD_LSUPER(0x8), // Windows key
            MOD_RCTRL(0x10),
            MOD_RSHIFT(0x20),
            MOD_RALT(0x40),
            MOD_RSUPER(0x80); // Windows key

            public final byte code;

            Mod(int code) {
                this.code = (byte) code;
            }
        }

        public enum Key {
            KEY_NONE(0x00),
            KEY_A(0X04),
            KEY_B(0X05),
            KEY_C(0X06),
            KEY_D(0X07),
            KEY_E(0X08),
            KEY_F(0X09),
            KEY_G(0X0A),
            KEY_H(0X0B),
            KEY_I(0X0C),
            KEY_J(0X0D),
            KEY_K(0X0E),
            KEY_L(0X0F),
            KEY_M(0X10),
            KEY_N(0X11),
            KEY_O(0X12),
            KEY_P(0X13),
            KEY_Q(0X14),
            KEY_R(0X15),
            KEY_S(0X16),
            KEY_T(0X17),
            KEY_U(0X18),
            KEY_V(0X19),
            KEY_W(0X1A),
            KEY_X(0X1B),
            KEY_Y(0X1C),
            KEY_Z(0X1D),
            KEY_D1(0X1E),
            KEY_D2(0X1F),
            KEY_D3(0X20),
            KEY_D4(0X21),
            KEY_D5(0X22),
            KEY_D6(0X23),
            KEY_D7(0X24),
            KEY_D8(0X25),
            KEY_D9(0X26),
            KEY_D0(0X27),
            KEY_ENTER(0X28),
            KEY_ESC(0X29),
            KEY_ESCAPE(0X29),
            KEY_BCKSPC(0X2A),
            KEY_BACKSPACE(0X2A),
            KEY_TAB(0X2B),
            KEY_SPACE(0X2C),
            KEY_MINUS(0X2D),
            KEY_DASH(0X2D),
            KEY_EQUALS(0X2E),
            KEY_EQUAL(0X2E),
            KEY_LBRACKET(0X2F),
            KEY_RBRACKET(0X30),
            KEY_BACKSLASH(0X31),
            KEY_HASH(0X32),
            KEY_NUMBER(0X32),
            KEY_SEMICOLON(0X33),
            KEY_QUOTE(0X34),
            KEY_BACKQUOTE(0X35),
            KEY_TILDE(0X35),
            KEY_COMMA(0X36),
            KEY_PERIOD(0X37),
            KEY_STOP(0X37),
            KEY_SLASH(0X38),
            KEY_CAPS_LOCK(0X39),
            KEY_CAPSLOCK(0X39),
            KEY_F1(0X3A),
            KEY_F2(0X3B),
            KEY_F3(0X3C),
            KEY_F4(0X3D),
            KEY_F5(0X3E),
            KEY_F6(0X3F),
            KEY_F7(0X40),
            KEY_F8(0X41),
            KEY_F9(0X42),
            KEY_F10(0X43),
            KEY_F11(0X44),
            KEY_F12(0X45),
            KEY_PRINT(0X46),
            KEY_SCROLL_LOCK(0X47),
            KEY_SCROLLLOCK(0X47),
            KEY_PAUSE(0X48),
            KEY_INSERT(0X49),
            KEY_HOME(0X4A),
            KEY_PAGEUP(0X4B),
            KEY_PGUP(0X4B),
            KEY_DEL(0X4C),
            KEY_DELETE(0X4C),
            KEY_END(0X4D),
            KEY_PAGEDOWN(0X4E),
            KEY_PGDOWN(0X4E),
            KEY_RIGHT(0X4F),
            KEY_LEFT(0X50),
            KEY_DOWN(0X51),
            KEY_UP(0X52),
            KEY_NUM_LOCK(0X53),
            KEY_NUMLOCK(0X53),
            KEY_KP_DIVIDE(0X54),
            KEY_KP_MULTIPLY(0X55),
            KEY_KP_MINUS(0X56),
            KEY_KP_PLUS(0X57),
            KEY_KP_ENTER(0X58),
            KEY_KP_RETURN(0X58),
            KEY_KP_1(0X59),
            KEY_KP_2(0X5A),
            KEY_KP_3(0X5B),
            KEY_KP_4(0X5C),
            KEY_KP_5(0X5D),
            KEY_KP_6(0X5E),
            KEY_KP_7(0X5F),
            KEY_KP_8(0X60),
            KEY_KP_9(0X61),
            KEY_KP_0(0X62),
            KEY_KP_PERIOD(0X63),
            KEY_KP_STOP(0X63),
            KEY_APPLICATION(0X65),
            KEY_POWER(0X66),
            KEY_KP_EQUALS(0X67),
            KEY_KP_EQUAL(0X67),
            KEY_F13(0X68),
            KEY_F14(0X69),
            KEY_F15(0X6A),
            KEY_F16(0X6B),
            KEY_F17(0X6C),
            KEY_F18(0X6D),
            KEY_F19(0X6E),
            KEY_F20(0X6F),
            KEY_F21(0X70),
            KEY_F22(0X71),
            KEY_F23(0X72),
            KEY_F24(0X73),
            KEY_EXECUTE(0X74),
            KEY_HELP(0X75),
            KEY_MENU(0X76),
            KEY_SELECT(0X77),
            KEY_CANCEL(0X78),
            KEY_REDO(0X79),
            KEY_UNDO(0X7A),
            KEY_CUT(0X7B),
            KEY_COPY(0X7C),
            KEY_PASTE(0X7D),
            KEY_FIND(0X7E),
            KEY_MUTE(0X7F),
            KEY_VOLUME_UP(0x80),
            KEY_VOLUME_DOWN(0x81);

            public final byte code;

            Key(int code) {
                this.code = (byte) code;
            }
        }
    }
}
