package com.shevchenko.staffapp.connectivity.protocols;

import java.io.IOException;

public interface IProtocolsDataManagement {
        public boolean startAudit();
        public void stopAudit();
        public void update(int deltaTime) throws IOException;
}
