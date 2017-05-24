package front_end_server;

import java.net.InetAddress;
import java.util.*;

public class Data {

    PriorityQueue<Client_Info> ranking;
    HashMap<InetAddress,Client_Info> registo;
    int burstSize;
    long timeout;

    public Data() {
       this.ranking = new PriorityQueue<Client_Info>(100,new ServerComparator());
       this.registo = new HashMap<>();
       this.burstSize = 100;
       this.timeout = 15000;
    }

    public void updateScore(Client_Info client_info) {
        if (registo.containsKey(client_info.getIp_address())) {
            Client_Info anterior = registo.get(client_info.getIp_address());
            ranking.remove(anterior);
        }
        client_info.setScore(
                client_info.getPacket_loss() * 0.3 +
                client_info.getRound_trip_time() * 0.5 +
                client_info.getTcp_connections() * 0.2
        );
        ranking.add(client_info);
        registo.put(client_info.getIp_address(),client_info);
    }

    public Client_Info nextserver(){
        return ranking.poll();
    }

    public void lostTCP(Client_Info client_info){
        client_info.setTcp_connections(client_info.getTcp_connections()-1);
        updateScore(client_info);
    }

    public void newTCP(Client_Info client_info){
        client_info.setTcp_connections(client_info.getTcp_connections()+1);
        updateScore(client_info);
    }

    public void update(Client_Info client_info, long rtt, int pl){

        client_info.setPacket_loss(pl);
        client_info.setRound_trip_time(rtt);
        updateScore(client_info);
    }

    public void removeClient(Client_Info ci) {
        ranking.remove(ci);
        registo.remove(ci);
    }

    public Client_Info getClientInfo(InetAddress inetAddress){ return this.registo.get(inetAddress); }

    public boolean existsClient(InetAddress ip_address) {
        return registo.containsKey(ip_address);
    }

    public PriorityQueue<Client_Info> getRanking() {
        return ranking;
    }

    public void setRanking(PriorityQueue<Client_Info> ranking) {
        this.ranking = ranking;
    }

    public HashMap<InetAddress, Client_Info> getRegisto() {
        return registo;
    }

    public void setRegisto(HashMap<InetAddress, Client_Info> registo) {
        this.registo = registo;
    }

    public int getBurstSize() {
        return burstSize;
    }

    public void setBurstSize(int burstSize) {
        this.burstSize = burstSize;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void updateLastLog(InetAddress ip_address, long timestamp) {
        Client_Info cliente;
        if (existsClient(ip_address)) {
            cliente = registo.get(ip_address);
            cliente.setLastLog(timestamp);
            registo.put(ip_address, cliente);
        }
    }

    public void addClient (Client_Info cliente) {
        if (!registo.containsKey(cliente.getIp_address())) {
            ranking.add(cliente);
            registo.put(cliente.getIp_address(), cliente);
        }
    }
}
