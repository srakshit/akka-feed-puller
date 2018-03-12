package com.dxc.mss.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import com.dxc.mss.Feed;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

/**
 * Created by rakshit on 11/03/2018.
 */
public class FeedsActor extends AbstractActor {
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
                .match(Feed.class, feed -> {
                    File file = new File("downloaded_files/" + feed.getCompany() + "_" + feed.getFeedName() + "_" + UUID.randomUUID());
                    URL url=new URL(feed.getUrl());
                    URLConnection conn = url.openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
                    conn.connect();
                    FileUtils.copyInputStreamToFile(conn.getInputStream(), file);;
                    getContext().parent().tell(new DownloadableFeedsActor.CompleteDownloadFeed(feed), ActorRef.noSender());
                    getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
                })
                .build();
    }
}
