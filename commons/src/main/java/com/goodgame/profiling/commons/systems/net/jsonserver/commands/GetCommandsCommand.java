package com.goodgame.profiling.commons.systems.net.jsonserver.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.commons.systems.net.jsonserver.Command;

@SuppressWarnings( "rawtypes" )
public class GetCommandsCommand< E extends Environment > implements Command<E> {

    private final List<Command> commands;

    public GetCommandsCommand() {
        commands = new ArrayList<>();
    }

    @Override
    public List<Class<? super E>> getRequiredEnvironments() {
        return Collections.emptyList();
    }

    @Override
    public String getJSONCommand() {
        return "get-commands";
    }

    @Override
    public List<Pair<String, Boolean>> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public JSONObject execute( JSONObject input, E environment ) {
        JSONArray cmdList = new JSONArray();
        for ( Command<?> command : commands ) {
            cmdList.put( makeJSONCommand( command ) );
        }
        return new JSONObject().put( "commands", cmdList );
    }

    private static < F extends Environment > JSONObject makeJSONCommand( Command<F> command ) {
        JSONArray params = new JSONArray();
        for ( Pair<String, Boolean> pair : command.getParameters() ) {
            JSONObject param = new JSONObject();
            param.put( "parameter", pair.getLeft() );
            param.put( "required", pair.getRight() );
            params.put( param );
        }
        JSONObject cmd = new JSONObject();
        cmd.put( "name", command.getJSONCommand() );
        cmd.put( "params", params );
        return cmd;
    }

    public void add( Command command ) {
        commands.add( command );
    }

}
