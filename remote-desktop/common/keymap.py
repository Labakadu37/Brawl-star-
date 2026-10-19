"""Map Tkinter key names (``event.keysym``) to pynput special keys.

The admin sends the Tkinter keysym and the printable character (if any).
The client types the character directly when it is printable, and falls
back to this table for the non-printable keys (arrows, Enter, Ctrl, ...).

pynput is imported lazily so the admin app, which never presses keys
locally, does not need pynput installed.
"""


def build_special_keys():
    from pynput.keyboard import Key

    return {
        "Return": Key.enter,
        "KP_Enter": Key.enter,
        "BackSpace": Key.backspace,
        "Tab": Key.tab,
        "Escape": Key.esc,
        "space": Key.space,
        "Delete": Key.delete,
        "Insert": Key.insert,
        "Home": Key.home,
        "End": Key.end,
        "Prior": Key.page_up,
        "Next": Key.page_down,
        "Left": Key.left,
        "Right": Key.right,
        "Up": Key.up,
        "Down": Key.down,
        "Caps_Lock": Key.caps_lock,
        "Num_Lock": Key.num_lock,
        "Scroll_Lock": Key.scroll_lock,
        "Print": Key.print_screen,
        "Pause": Key.pause,
        "Menu": Key.menu,
        "Shift_L": Key.shift_l,
        "Shift_R": Key.shift_r,
        "Control_L": Key.ctrl_l,
        "Control_R": Key.ctrl_r,
        "Alt_L": Key.alt_l,
        "Alt_R": Key.alt_gr,
        "Super_L": Key.cmd,
        "Super_R": Key.cmd_r,
        "Win_L": Key.cmd,
        "Win_R": Key.cmd_r,
        "F1": Key.f1, "F2": Key.f2, "F3": Key.f3, "F4": Key.f4,
        "F5": Key.f5, "F6": Key.f6, "F7": Key.f7, "F8": Key.f8,
        "F9": Key.f9, "F10": Key.f10, "F11": Key.f11, "F12": Key.f12,
    }
