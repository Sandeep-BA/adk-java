/*
 * Copyright 2025 Google LLC
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
package com.google.adk.models.langchain4j;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import java.util.ArrayList;
import java.util.List;

public class RunLoop {
  public static List<Event> askAgent(BaseAgent agent, Object... messages) {
    return runLoop(agent, false, messages);
  }

  public static List<Event> askAgentStreaming(BaseAgent agent, Object... messages) {
    return runLoop(agent, true, messages);
  }

  public static List<Event> runLoop(BaseAgent agent, boolean streaming, Object... messages) {
    ArrayList<Event> allEvents = new ArrayList<>();

    Runner runner = new InMemoryRunner(agent, agent.name());
    Session session = runner.sessionService().createSession(agent.name(), "user132").blockingGet();

    for (Object message : messages) {
      Content messageContent = null;
      if (message instanceof String) {
        messageContent = Content.fromParts(Part.fromText((String) message));
      } else if (message instanceof Part) {
        messageContent = Content.fromParts((Part) message);
      } else if (message instanceof Content) {
        messageContent = (Content) message;
      }
      allEvents.addAll(
          runner
              .runAsync(
                  session,
                  messageContent,
                  RunConfig.builder()
                      .setStreamingMode(
                          streaming ? RunConfig.StreamingMode.SSE : RunConfig.StreamingMode.NONE)
                      .build())
              .blockingStream()
              .toList());
    }

    return allEvents;
  }
}
