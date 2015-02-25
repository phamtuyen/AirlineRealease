package com.mbv.ticketsystem.airline.jetstar;


import com.mbv.ticketsystem.airline.AbstractAirWorker;
import com.mbv.ticketsystem.airline.AbstractWorkerFactory;

@SuppressWarnings("serial")
public class JetstarWorkerFactory extends AbstractWorkerFactory {
    private JetstarAccount account;

    public JetstarAccount getAccount() {
        return account;
    }

    public void setAccount(JetstarAccount account) {
        this.account = account;
    }

    @Override
    protected AbstractAirWorker doCreate() {
        return new JetstarWorker(account);
    }
}
