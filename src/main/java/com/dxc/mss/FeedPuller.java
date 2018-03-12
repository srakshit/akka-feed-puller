package com.dxc.mss;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.dxc.mss.actors.SchedulerActor;

/**
 * Created by rakshit on 10/03/2018.
 */
public class FeedPuller {
    public static void main(String[] args) throws java.io.IOException {
        ActorSystem system = ActorSystem.create("feed-puller");
        ActorRef feedConfigActor = system.actorOf(Props.create(SchedulerActor.class), "feed-puller-scheduler");
        feedConfigActor.tell("start-scheduler", ActorRef.noSender());
    }
}
