package utils;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AllureEdit {
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static String getStepUUID() {
        return Allure.getLifecycle().getCurrentTestCaseOrStep().get();
    }

    public static void setParams(Map<String, String> params) {
        String uuid = getStepUUID();
        List<Parameter> parameters = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            parameters.add(new Parameter().setName(entry.getKey()).setValue(entry.getValue()));
        }
        Allure.getLifecycle().updateStep(uuid, stepResult -> stepResult.setParameters(parameters));
    }

    public static void removeParamByName(String paramName) {
        String uuid = getStepUUID();
        Allure.getLifecycle().updateStep(uuid, stepResult -> {
            Parameter p = stepResult.getParameters().stream().filter(a -> a.getName().equals(paramName)).toList().get(0);
            stepResult.getParameters().remove(p);
        });
    }
}
