package com.dxc.mss.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.dxc.mss.Feed;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by rakshit on 10/03/2018.
 */
public class FeedConfigActor extends AbstractActor {
    private List<Feed> feeds = new ArrayList<>();
    private ActorRef downloadableFeedActor = getContext().actorOf(Props.create(DownloadableFeedsActor.class), "downloadable-feeds");;

    public static class FeedLastUpdated {
        public final int id;
        public final Date lastUpdated;

        public FeedLastUpdated(int id, Date lastUpdated){
            this.id = id;
            this.lastUpdated = lastUpdated;
        }
    }

    @Override
    public void preStart() {
        System.out.println(getSelf() + " started");
    }

    @Override
    public void postStop() {
        System.out.println(getSelf() + " stopped");
    }

    @Override
    public Receive createReceive(){
        return receiveBuilder()
                .matchEquals("read-feed-config", feed -> {
                    System.out.println("Reading feed config");
                    byte[] jsonData = Files.readAllBytes(Paths.get("feedConfig.json"));
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                    feeds = objectMapper.readValue(jsonData, new TypeReference<List<Feed>>(){});

                    for (Feed f: feeds)
                        System.out.println(f);
                    System.out.println();

                    downloadableFeedActor.tell(new DownloadableFeedsActor.InitiateDownloadFeed(feeds), ActorRef.noSender());
                })
                .match(FeedLastUpdated.class, f -> {
                    Feed feed = feeds.get(f.id - 1);
                    System.out.println("Updating " + feed.getCompany() + "-" + feed.getFeedName() +" config");
                    feed.setLastUpdated(f.lastUpdated);
                    feeds.set(f.id - 1, feed);

                    ObjectMapper mapper = new ObjectMapper();
                    ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
                    writer.writeValue(new File("feedConfig.json"), feeds);
                })
                .build();
    }
}
