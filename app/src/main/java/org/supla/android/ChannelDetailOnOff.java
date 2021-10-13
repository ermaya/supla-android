package org.supla.android;

/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.lib.SuplaTimerState;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ChannelDetailOnOff extends DetailLayout implements View.OnClickListener {

    AppCompatButton btnTimerArm;
    AppCompatEditText etDurationMS;
    AppCompatCheckBox cbOn;
    Timer countdownTimer;
    TextView countdownInfo;

    public ChannelDetailOnOff(Context context, ChannelListView cLV) {
        super(context, cLV);
    }

    public ChannelDetailOnOff(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelDetailOnOff(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChannelDetailOnOff(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        super.init();
        btnTimerArm = findViewById(R.id.btnTimerArm);
        etDurationMS = findViewById(R.id.etDurationMS);
        cbOn = findViewById(R.id.cbOn);
        countdownInfo = findViewById(R.id.tvCountdownInfo);

        btnTimerArm.setOnClickListener(this);
    }

    private Channel getCahnnel() {
        ChannelBase channelBase = getChannelFromDatabase();
        return channelBase instanceof Channel ? (Channel)channelBase : null;
    }

    private SuplaTimerState getTimerState() {
        Channel channel = getCahnnel();
        if (channel == null) {
            return null;
        }

        if ((getCahnnel().getFlags()
                & SuplaConst.SUPLA_CHANNEL_FLAG_COUNTDOWN_TIMER_SUPPORTED) == 0) {
            return null;
        }

        ChannelExtendedValue cev = channel.getExtendedValue();

        if (cev != null) {
            return cev.getExtendedValue().TimerStateValue;
        }

        return null;
    }

    private boolean expectedValueHasBeenReached() {
        SuplaTimerState state = getTimerState();
        if (state == null) {
            return false;
        }

        Channel channel = getCahnnel();
        if (channel == null) {
            return false;
        }

        return channel.getValue().hiValue() == state.expectedHiValue();
    }

    private boolean remoteCountdownTimerStarted() {
        Date now = new Date();
        SuplaTimerState state = getTimerState();
        if (state == null) {
            return false;
        }

        Date countdownEndsAt = state.getCountdownEndsAt();

        return countdownEndsAt == null ? false : now.before(countdownEndsAt);
    }

    @Override
    public View inflateContentView() {
        return inflateLayout(R.layout.detail_onoff);
    }

    private String countdownTimeFormat(int timeMS) {
        if (timeMS < 0) {
            timeMS = 0;
        }

        return Integer.toString(timeMS/1000)+"s.";
    }

    private void startTheCountdown() {
        if (countdownTimer == null) {
            countdownTimer = new Timer();

            countdownTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (getContext() instanceof Activity) {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (remoteCountdownTimerStarted()) {
                                    if (expectedValueHasBeenReached()) {
                                        countdownInfo.setText("");
                                    } else {
                                        SuplaTimerState state = getTimerState();
                                        if (state != null) {
                                            Date now = new Date();
                                            int diff = (int)(state.getCountdownEndsAt().getTime()
                                                    - now.getTime());
                                            countdownInfo.setText(countdownTimeFormat(diff));
                                        }
                                    }
                                } else {
                                    countdownTimer.cancel();
                                    countdownTimer = null;
                                    countdownInfo.setText("");
                                }
                            }
                        });
                    }
                }
            }, 500, 1000);
        }
    }

    @Override
    public void OnChannelDataChanged() {

        if (remoteCountdownTimerStarted()) {
            startTheCountdown();
        }
    }

    @Override
    public void onClick(View view) {
        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        if (client != null) {
            int timeMS = Integer.parseInt(etDurationMS.getText().toString());
            countdownInfo.setText("");

            client.timerArm(getRemoteId(), cbOn.isChecked(),
                    timeMS);
        }
    }
}
