package com.mytlogos.enterprise.test;

import org.joda.time.DateTime;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SimpleTest {
    public static void main(String[] args) throws Exception {
//        Client client = new Client();
//        Call<ClientUser> call = client.login("mater", "123");
//        Response<ClientUser> response = call.execute();
//        System.out.println(response.body());

        CompletableFuture<Void> future = CompletableFuture.completedFuture(null).thenRun(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(DateTime.now() + "future:" + Thread.currentThread());
        });
        System.out.println(DateTime.now() + "after: " + Thread.currentThread());
    }

    private static void d(Iterable<Integer> integers) {

    }

    private static class A<T> {
        List<T> value;

        void hello(List<T> list) {
            for (T t : list) {
                System.out.println(t);
            }
        }
    }

    private static class B extends A {
    }

}
