package com.dxc.mss.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.dxc.mss.Feed;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by rakshit on 11/03/2018.
 */
public class DownloadableFeedsActor extends AbstractActor {
    private ActorRef feedsActor;
    private Map<String, List<String>> downloadFeedsMap = new HashMap<>();

    public static class InitiateDownloadFeed {
        private final List<Feed> feeds;

        public InitiateDownloadFeed(List<Feed> feeds){
            this.feeds = feeds;
        }
    }

    public static class CompleteDownloadFeed {
        private final Feed feed;

        public CompleteDownloadFeed(Feed feed){
            this.feed = feed;
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
    public Receive createReceive() {
        return receiveBuilder()
                .match(InitiateDownloadFeed.class, idf -> {
                    System.out.println("Refining feeds");

                    List<Feed> feeds = idf.feeds.stream().filter(x -> (new Date().getTime() - x.getLastUpdated().getTime()) > 120000L).collect(Collectors.toList());
                    for (Feed f: feeds) {
                        if (!downloadFeedsMap.containsKey(f.getCompany())) {
                            System.out.println("Download initiated for " + f.getCompany() + ":" + f.getFeedName());
                            List<String> values=new ArrayList<>();
                            values.add(f.getFeedName());
                            downloadFeedsMap.put(f.getCompany(), values);

                            //Send download message
                            feedsActor = getContext().actorOf(Props.create(FeedsActor.class));
                            feedsActor.tell(f, ActorRef.noSender());
                        }else {
                            if (downloadFeedsMap.get(f.getCompany()).contains(f.getFeedName())) {
                                System.out.println("Already downloading " + f.getCompany() + ":" + f.getFeedName());
                            }else if (downloadFeedsMap.get(f.getCompany()).size() < 2) {
                                System.out.println("Download initiated for " + f.getCompany() + ":" + f.getFeedName());
                                downloadFeedsMap.get(f.getCompany()).add(f.getFeedName());

                                //Send download message
                                feedsActor = getContext().actorOf(Props.create(FeedsActor.class));
                                feedsActor.tell(f, ActorRef.noSender());
                            }else {
                                System.out.println("Download skipped for " + f.getCompany() + ":" + f.getFeedName());
                            }
                        }
                    }
                })
                .match(CompleteDownloadFeed.class, cdf -> {
                    System.out.println("Download completed for " + cdf.feed.getCompany() + ":" + cdf.feed.getFeedName());
                    downloadFeedsMap.get(cdf.feed.getCompany()).remove(cdf.feed.getFeedName());
                    getContext().parent().tell(new FeedConfigActor.FeedLastUpdated(cdf.feed.getId(), new Date()), ActorRef.noSender());
                })
                .build();
    }
}
