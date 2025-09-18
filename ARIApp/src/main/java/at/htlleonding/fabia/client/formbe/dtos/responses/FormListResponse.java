package at.htlleonding.fabia.client.formbe.dtos.responses;

import at.htlleonding.fabia.client.formbe.dtos.FormListDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class FormListResponse {
    private List<FormListDto> forms;

    public FormListResponse() {}

    public FormListResponse(List<FormListDto> forms) {
        this.forms = forms;
    }
}
