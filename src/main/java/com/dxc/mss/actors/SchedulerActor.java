package com.dxc.mss.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by rakshit on 10/03/2018.
 */
public class SchedulerActor extends AbstractActor {
    private ActorRef feedConfigActor = getContext().actorOf(Props.create(FeedConfigActor.class), "feed-config-manager");

    @Override
    public void preStart() {
        System.out.println(getSelf() + " started");
    }

    @Override
    public void postStop() {
        System.out.println(getSelf() + " stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("start-scheduler", schedule -> {
                    TimerTask repeatedTask = new TimerTask() {
                        @Override
                        public void run(){
                            feedConfigActor.tell("read-feed-config", ActorRef.noSender());
                        }
                    };
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(repeatedTask, 1000L, 60000L);
                })
                .build();
    }
}
