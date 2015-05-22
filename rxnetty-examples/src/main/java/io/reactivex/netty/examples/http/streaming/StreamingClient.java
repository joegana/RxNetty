/*
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.reactivex.netty.examples.http.streaming;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.examples.AbstractClientExample;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;

import java.nio.charset.Charset;

/**
 * An HTTP streaming example. There are three ways of running this example:
 *
 * <h2>Default</h2>
 *
 * The default way is to just run this class with no arguments, which will start a server ({@link StreamingServer}) on
 * an ephemeral port and then send an HTTP request to that server and print the response.
 *
 * <h2>After starting {@link StreamingServer}</h2>
 *
 * If you want to see how {@link StreamingServer} work, you can run {@link StreamingServer} by yourself and then pass
 * the port on which the server started to this class as a program argument:
 *
 <PRE>
 java io.reactivex.netty.examples.http.streaming.StreamingClient [server port]
 </PRE>
 *
 * <h2>Existing HTTP server</h2>
 *
 * You can also use this client to send a GET request "/stream" to an existing HTTP server (different than
 * {@link StreamingServer}) by passing the port fo the existing server similar to the case above:
 *
 <PRE>
 java io.reactivex.netty.examples.http.streaming.StreamingClient [server port]
 </PRE>
 *
 * In all the above usages, this client will print the response received from the server.
 */
public class StreamingClient extends AbstractClientExample {

    public static void main(String[] args) {

        /*
         * Retrieves the server port, using the following algorithm:
         * <ul>
             <li>If an argument is passed, then use the argument as the server port.</li>
             <li>Otherwise, see if the server in the passed server class is already running. If so, use that port.</li>
             <li>Otherwise, start the passed server class and use that port.</li>
         </ul>
         */
        int port = getServerPort(StreamingServer.class, args);

        HttpClient.newClient("localhost", port) /*Create a client*/
                  .createGet("/stream") /*Creates a GET request with URI "/hello"*/
                  .doOnNext(resp -> logger.info(resp.toString()))/*Prints the response headers*/
                  .flatMap((HttpClientResponse<ByteBuf> resp) -> /*Return the stream to response content stream.*/
                                     /*Now use the content stream.*/
                                     resp.getContent()
                                         /*Convert ByteBuf to string for each content*/
                                         .map(bb -> bb.toString(Charset.defaultCharset()))
                  )
                  /*Since, the server sends an infinite stream, take only 10 items*/
                  .take(10)
                  /*Block till the response comes to avoid JVM exit.*/
                  .toBlocking()
                  /*Print each content chunk*/
                  .forEach(logger::info);
    }
}