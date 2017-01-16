package cf.netdex.hidfuzzer;

/**
 * Created by netdex on 1/15/2017.
 */

public class Input {

    static class Mouse {
        enum Buttons {
            BUTTON_LEFT(0x1),
            BUTTON_RIGHT(0x2),
            BUTTON_MIDDLE(0x4);
            public byte code;

            Buttons(int code) {
                this.code = (byte) code;
            }
        }
    }

    static class Keyboard {
        enum ModifierMask {
            LCTRL(0x1),
            LSHIFT(0x2),
            LALT(0x4),
            LSUPER(0x8), // Windows key
            RCTRL(0x10),
            RSHIFT(0x20),
            RALT(0x40),
            RSUPER(0x80); // Windows key

            public byte code;

            ModifierMask(int code) {
                this.code = (byte) code;
            }
        }

        enum Key {
            A(0X04),
            B(0X05),
            C(0X06),
            D(0X07),
            E(0X08),
            F(0X09),
            G(0X0A),
            H(0X0B),
            I(0X0C),
            J(0X0D),
            K(0X0E),
            L(0X0F),
            M(0X10),
            N(0X11),
            O(0X12),
            P(0X13),
            Q(0X14),
            RANDOM(0X15),
            S(0X16),
            T(0X17),
            U(0X18),
            V(0X19),
            W(0X1A),
            X(0X1B),
            Y(0X1C),
            Z(0X1D),
            D1(0X1E),
            D2(0X1F),
            D3(0X20),
            D4(0X21),
            D5(0X22),
            D6(0X23),
            D7(0X24),
            D8(0X25),
            D9(0X26),
            D0(0X27),
            ENTER(0X28),
            ESC(0X29),
            ESCAPE(0X29),
            BCKSPC(0X2A),
            BACKSPACE(0X2A),
            TAB(0X2B),
            SPACE(0X2C),
            MINUS(0X2D),
            DASH(0X2D),
            EQUALS(0X2E),
            EQUAL(0X2E),
            LBRACKET(0X2F),
            RBRACKET(0X30),
            BACKSLASH(0X31),
            HASH(0X32),
            NUMBER(0X32),
            SEMICOLON(0X33),
            QUOTE(0X34),
            BACKQUOTE(0X35),
            TILDE(0X35),
            COMMA(0X36),
            PERIOD(0X37),
            STOP(0X37),
            SLASH(0X38),
            CAPS_LOCK(0X39),
            CAPSLOCK(0X39),
            F1(0X3A),
            F2(0X3B),
            F3(0X3C),
            F4(0X3D),
            F5(0X3E),
            F6(0X3F),
            F7(0X40),
            F8(0X41),
            F9(0X42),
            F10(0X43),
            F11(0X44),
            F12(0X45),
            PRINT(0X46),
            SCROLL_LOCK(0X47),
            SCROLLLOCK(0X47),
            PAUSE(0X48),
            INSERT(0X49),
            HOME(0X4A),
            PAGEUP(0X4B),
            PGUP(0X4B),
            DEL(0X4C),
            DELETE(0X4C),
            END(0X4D),
            PAGEDOWN(0X4E),
            PGDOWN(0X4E),
            RIGHT(0X4F),
            LEFT(0X50),
            DOWN(0X51),
            UP(0X52),
            NUM_LOCK(0X53),
            NUMLOCK(0X53),
            KP_DIVIDE(0X54),
            KP_MULTIPLY(0X55),
            KP_MINUS(0X56),
            KP_PLUS(0X57),
            KP_ENTER(0X58),
            KP_RETURN(0X58),
            KP_1(0X59),
            KP_2(0X5A),
            KP_3(0X5B),
            KP_4(0X5C),
            KP_5(0X5D),
            KP_6(0X5E),
            KP_7(0X5F),
            KP_8(0X60),
            KP_9(0X61),
            KP_0(0X62),
            KP_PERIOD(0X63),
            KP_STOP(0X63),
            APPLICATION(0X65),
            POWER(0X66),
            KP_EQUALS(0X67),
            KP_EQUAL(0X67),
            F13(0X68),
            F14(0X69),
            F15(0X6A),
            F16(0X6B),
            F17(0X6C),
            F18(0X6D),
            F19(0X6E),
            F20(0X6F),
            F21(0X70),
            F22(0X71),
            F23(0X72),
            F24(0X73),
            EXECUTE(0X74),
            HELP(0X75),
            MENU(0X76),
            SELECT(0X77),
            CANCEL(0X78),
            REDO(0X79),
            UNDO(0X7A),
            CUT(0X7B),
            COPY(0X7C),
            PASTE(0X7D),
            FIND(0X7E),
            MUTE(0X7F),
            VOLUME_UP(0x80),
            VOLUME_DOWN(0x81);

            public byte code;

            Key(int code) {
                this.code = (byte) code;
            }
        }
    }
}
