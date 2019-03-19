package com.mytlogos.enterprise.test;

import com.mytlogos.enterprise.background.api.Client;
import com.mytlogos.enterprise.background.api.model.ClientUser;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class SimpleTest {
    public static void main(String[] args) throws IOException {
        Client client = new Client();
        Call<ClientUser> call = client.login("mater", "123");
        Response<ClientUser> response = call.execute();
        System.out.println(response.body());
    }
}
