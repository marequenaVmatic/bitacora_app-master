package com.shevchenko.staffapp.connectivity.protocols.dex;


import com.shevchenko.staffapp.connectivity.protocols.IProtocolsDataManagement;
import com.shevchenko.staffapp.connectivity.protocols.statemachine.Event;
import com.shevchenko.staffapp.connectivity.protocols.statemachine.IEventSync;
import com.shevchenko.staffapp.connectivity.protocols.statemachine.StateBase;

public class InitialState<AI extends IProtocolsDataManagement>
    extends StateBase<AI> implements IProtocolsDataManagement
{

    public static final Event START_PAIRING = new Event("START_PAIRING", 0);

    public InitialState(AI automation, IEventSync eventSync) {
        super(automation, eventSync);

    }

    @Override
    public boolean startAudit() {
        logger.log("Pairing...");
        castEvent(START_PAIRING);
        return true;
    }

    @Override
    public void stopAudit() {
    }

    @Override
    public void update(int deltaTime) {
    }

}
