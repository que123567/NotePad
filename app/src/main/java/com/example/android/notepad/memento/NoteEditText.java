package com.example.android.notepad.memento;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by smaug(Qiuzhonghao) on 2017/5/18.
 */

public class NoteEditText extends android.support.v7.widget.AppCompatEditText {
    public NoteEditText(Context context) {
        super(context);
    }

    public NoteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoteEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Memento mementoFactory() { //
        Memento noteMemento = new Memento();
        noteMemento.setText(getText().toString());
        noteMemento.setCursor(getSelectionStart());
        return noteMemento;
    }

    public void restore(Memento memento) { //撤销
        setText(memento.getText());
        setSelection(memento.getCursor());
    }
}
