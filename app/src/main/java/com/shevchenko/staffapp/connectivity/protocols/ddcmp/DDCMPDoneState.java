package com.shevchenko.staffapp.connectivity.protocols.ddcmp;

import com.shevchenko.staffapp.connectivity.protocols.IProtocolsDataManagement;
import com.shevchenko.staffapp.connectivity.protocols.statemachine.IEventSync;
import com.shevchenko.staffapp.connectivity.protocols.statemachine.StateBase;

import java.io.IOException;

public class DDCMPDoneState<AI extends IProtocolsDataManagement>
        extends StateBase<AI> implements IProtocolsDataManagement{


    public DDCMPDoneState(AI automation, IEventSync eventSync) {
        super(automation, eventSync);
    }

    @Override
    public boolean startAudit() {
        return false;
    }

    @Override
    public void stopAudit() {

    }

    @Override
    public void update(int deltaTime) throws IOException {

    }
}
