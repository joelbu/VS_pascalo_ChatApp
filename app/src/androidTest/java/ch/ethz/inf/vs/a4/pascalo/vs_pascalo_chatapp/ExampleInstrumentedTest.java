package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.ChatParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedChatMap;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp", appContext.getPackageName());
    }

    @Test
    public void chatSerializerTest() throws Exception {
        ChatParser chatParser = new ChatParser();
        Map<UUID, Chat> chats = new HashMap<UUID, Chat>();

        UUID uuid = UUID.fromString("f590e29d-6315-4572-a2bc-5009a3ac1251");
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(1480344165822L);
        Chat chat = new Chat(uuid, "Hans Muster", "", 0, calendar);
        chat.addMessage(new Message(true, true, GregorianCalendar.getInstance(),
                new VectorClock(1, 4), "Text?"));
        chat.addMessage(new Message(true, true, GregorianCalendar.getInstance(),
                new VectorClock(5, 5), "Text6"));
        chats.put(uuid, chat);


        UUID uuid1 = UUID.fromString("7446c9b8-209b-467a-ae27-80e419381722");
        GregorianCalendar calendar1 = new GregorianCalendar();
        calendar1.setTimeInMillis(1480344165828L);
        Chat chat1 = new Chat(uuid1, "Max Problem", "", 0, calendar1);
        chat.addMessage(new Message(false, false, GregorianCalendar.getInstance(),
                new VectorClock(0, 0), "test"));
        chat.addMessage(new Message(true, false, GregorianCalendar.getInstance(),
                new VectorClock(1, 1), "ack"));
        chats.put(uuid1, chat1);


        assertEquals(
                "{\"chats\":" +
                        "[" +
                            "{\"chatPartnerID\":\"f590e29d-6315-4572-a2bc-5009a3ac1251\"," +
                                "\"chatPartnerName\":\"Hans Muster\"," +
                                "\"chatPartnerPublicKey\":\"\"," +
                                "\"unreadMessages\":0," +
                                "\"recentActivity\":1480344165822}," +
                            "{\"chatPartnerID\":\"7446c9b8-209b-467a-ae27-80e419381722\"," +
                                "\"chatPartnerName\":\"Max Problem\"," +
                                "\"chatPartnerPublicKey\":\"\"," +
                                "\"unreadMessages\":0," +
                                "\"recentActivity\":1480344165828}" +
                        "]" +
                "}",
                chatParser.serializeCollectionOfChats(chats.values()).toString()
        );
    }

    @Test
    public void chatParserTest() throws Exception {
        String input =
                "{\"chats\":" +
                        "[" +
                            "{\"chatPartnerID\":\"f590e29d-6315-4572-a2bc-5009a3ac1251\"," +
                                "\"chatPartnerName\":\"Hans Muster\"," +
                                "\"chatPartnerPublicKey\":\"\"," +
                                "\"unreadMessages\":0," +
                                "\"recentActivity\":1480344165824}," +
                            "{\"chatPartnerID\":\"7446c9b8-209b-467a-ae27-80e419381722\"," +
                                "\"chatPartnerName\":\"Max Problem\"," +
                                "\"chatPartnerPublicKey\":\"\"," +
                                "\"unreadMessages\":0," +
                                "\"recentActivity\":1480344165828}" +
                        "]" +
                "}";

        ChatsHolder chatsHolder = new ChatsHolder();
        ChatParser chatParser = new ChatParser();

        ParsedChatMap result = chatParser.parseMapOfChats(new JSONObject(input));
        assertEquals("Max Problem", result.chat
                .get(UUID.fromString("7446c9b8-209b-467a-ae27-80e419381722"))
                .getChatPartnerName()
        );

        assertEquals(1480344165824L, result.chat
                .get(UUID.fromString("f590e29d-6315-4572-a2bc-5009a3ac1251"))
                .getRecentActivity().getTimeInMillis()
        );

    }
}
