package com.kafka.producer;

import javax.swing.*;

public class WindowsPopUp {

    // windows 팝업 창 띄우는함수
    public void errorPopUp (String msg) {
        JOptionPane.showMessageDialog(null, msg, "ERROR", JOptionPane.ERROR_MESSAGE);
    }

}
