/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.fhir.demo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.mapping.StructureMapExtractionContext
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import java.util.UUID
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.StructureMap
import org.hl7.fhir.r4.utils.StructureMapUtilities

/** ViewModel for patient registration screen {@link AddPatientFragment}. */
class AddPatientViewModel(application: Application, private val state: SavedStateHandle) :
  AndroidViewModel(application) {

  val questionnaire: String
    get() = getQuestionnaireJson()
  val isPatientSaved = MutableLiveData<Boolean>()

  private val questionnaireResource: Questionnaire
    get() =
      FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaire) as
        Questionnaire
  private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)
  private var questionnaireJson: String? = null

  /**
   * Saves patient registration questionnaire response into the application database.
   *
   * @param questionnaireResponse patient registration questionnaire response
   */
  fun savePatient(questionnaireResponse: QuestionnaireResponse) {
    viewModelScope.launch {
      val structureMapString = """{
  "resourceType": "StructureMap",
  "id": "emcarea.registration.p",
  "meta": {
    "versionId": "76",
    "lastUpdated": "2022-08-02T07:27:00.865+00:00",
    "source": "#ltidV5WW82ThHnzg"
  },
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><pre>map &quot;https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureMap/emcarea.registration.p&quot; = &quot;emcarea.registration.p&quot;\r\n\r\n// emcarearegistrationp:https://fhir.dk.swisstph-mis.ch/matchbox/fhir/Questionnaire/emcarea.registration.p\r\n\r\nuses &quot;http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaireresponse&quot; alias 'questionnaireResponse' as source // Bundle are require where there is multiple ressource to be mapped\r\nuses &quot;http://hl7.org/fhir/StructureDefinition/Bundle&quot; alias 'bundle' as target // target that will be inserted in the bundle\r\nuses &quot;http://hl7.org/fhir/StructureDefinition/Patient&quot; alias 'Patient' as target // target that will be inserted in the bundle\r\nuses &quot;http://hl7.org/fhir/StructureDefinition/Encounter&quot; alias 'Encounter' as target // target that will be inserted in the bundle\r\nuses &quot;http://hl7.org/fhir/StructureDefinition/RelatedPerson&quot; alias 'RelatedPerson' as target // target that will be inserted in the bundle\r\nuses &quot;http://hl7.org/fhir/StructureDefinition/CommunicationRequest&quot; alias 'CommunicationRequest' as target\r\n\r\ngroup bundleMapping(source src : questionnaireResponse, target bundle : Bundle) {\r\n  src -&gt; bundle.id = uuid() &quot;id&quot;;\r\n  src -&gt; bundle.type = 'batch' &quot;type&quot;;\r\n  src -&gt;  uuid() as emcarepatientemcarearegistrationpid,  uuid() as emcareencounteremcarearegistrationpid,  uuid() as emcarerelatedpersoncaregiveremcarearegistrationpid,  uuid() as emcarecommunicationrequestemcarearegistrationpid then {\r\n    src -&gt;  bundle.entry as entry,  entry.fullUrl = append('urn:uuid:', emcarepatientemcarearegistrationpid),  entry.request as request,  request.method = 'PUT',  request.url = append('Patient/', emcarepatientemcarearegistrationpid),  entry.resource = create('Patient') as tgt then emcarepatientemcarearegistrationpgroup(src, tgt, emcarepatientemcarearegistrationpid) &quot;emcarepatientemcarearegistrationpgrouprule&quot;;\r\n    src -&gt;  bundle.entry as entry,  entry.fullUrl = append('urn:uuid:', emcareencounteremcarearegistrationpid),  entry.request as request,  request.method = 'PUT',  request.url = append('Encounter/', emcareencounteremcarearegistrationpid),  entry.resource = create('Encounter') as tgt then emcareencounteremcarearegistrationpgroup(src, tgt, emcareencounteremcarearegistrationpid) &quot;emcareencounteremcarearegistrationpgrouprule&quot;;\r\n    src -&gt;  bundle.entry as entry,  entry.fullUrl = append('urn:uuid:', emcarerelatedpersoncaregiveremcarearegistrationpid),  entry.request as request,  request.method = 'PUT',  request.url = append('RelatedPerson/', emcarerelatedpersoncaregiveremcarearegistrationpid),  entry.resource = create('RelatedPerson') as tgt then emcarerelatedpersoncaregiveremcarearegistrationpgroup(src, tgt, emcarerelatedpersoncaregiveremcarearegistrationpid) &quot;emcarerelatedpersoncaregiveremcarearegistrationpgrouprule&quot;;\r\n    src -&gt;  bundle.entry as entry,  entry.fullUrl = append('urn:uuid:', emcarecommunicationrequestemcarearegistrationpid),  entry.request as request,  request.method = 'PUT',  request.url = append('CommunicationRequest/', emcarecommunicationrequestemcarearegistrationpid),  entry.resource = create('CommunicationRequest') as tgt then emcarecommunicationrequestemcarearegistrationpgroup(src, tgt, emcarecommunicationrequestemcarearegistrationpid) &quot;emcarecommunicationrequestemcarearegistrationpgrouprule&quot;;\r\n  } &quot;gen-ids&quot;;\r\n}\r\n\r\ngroup emcarepatientemcarearegistrationpgroup(source src : questionnaireResponse, target tgt : Patient, source resid) {\r\n  src -&gt;  tgt.id = resid,  tgt.meta = create('Meta') as newMeta,  newMeta.profile = 'https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/patient' &quot;set-uuid&quot;;\r\n  src.item as item where linkId = 'EmCare.A.DE01' -&gt; tgt then {\r\n    item.answer as a -&gt; tgt.identifier = create('Identifier') as identifier then {\r\n      a -&gt;  identifier.value = a,  identifier.system = 'https://fhir.dk.swisstph-mis.ch/matchbox/fhir//CodeSystem/em-custom' &quot;id&quot;;\r\n    } &quot;set-id&quot;;\r\n  } &quot;DE01-main&quot;;\r\n  src.item as item where linkId = 'EmCare.A.DE02' -&gt; tgt then {\r\n    item.answer as a -&gt; tgt.identifier = create('Identifier') as identifier then {\r\n      a -&gt;  identifier.value = a,  identifier.system = 'https://fhir.dk.swisstph-mis.ch/matchbox/fhir//CodeSystem/em-custom' &quot;idu&quot;;\r\n    } &quot;set-id&quot;;\r\n  } &quot;DE02-main&quot;;\r\n  src.item as item where linkId = 'EmCare.A.DE03' -&gt; tgt then {\r\n    item.answer as a -&gt; tgt.identifier = create('Identifier') as identifier then {\r\n      a -&gt;  identifier.value = 'noid',  identifier.system = 'https://fhir.dk.swisstph-mis.ch/matchbox/fhir//CodeSystem/em-custom' &quot;noid&quot;;\r\n    } &quot;set-id&quot;;\r\n  } &quot;DE03-main&quot;;\r\n  src.item first as item where (linkId = 'EmCare.A.DE04') or (linkId = 'EmCare.A.DE05') or (linkId = 'EmCare.A.DE06') -&gt;  tgt as target,  target.name as name then SetOfficalGivenNameemcareade04(src, name) &quot;name-main&quot;;\r\n  src.item as item where linkId = 'EmCare.A.DE08' -&gt; tgt then {\r\n    item.answer as a -&gt; tgt.birthDate = a &quot;DE08-1&quot;;\r\n  } &quot;DE08-main&quot;;\r\n  src.item as item where linkId = 'EmCare.A.DE12' -&gt; tgt then {\r\n    item.answer as a -&gt;  tgt.extension as ext,  ext.url = 'https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/birthDateEstimator',  ext.valueCode = a &quot;DE12-1&quot;;\r\n  } &quot;DE12-main&quot;;\r\n  src.item as item where linkId = 'EmCare.A.DE16' -&gt; tgt then {\r\n    item.answer as a -&gt; tgt.gender = translate(a, 'sex-of-the-client', 'http://hl7.org/fhir/administrative-gender') &quot;DE16-1&quot;;\r\n  } &quot;DE16-main&quot;;\r\n  src.item as item where linkId = 'EmCare.A.DE20' -&gt; tgt then {\r\n    item.answer as a -&gt;  tgt.address as address,  address.text = a &quot;DE20-1&quot;;\r\n  } &quot;DE20-main&quot;;\r\n  src.item as item where linkId = 'EmCare.A.DE47' -&gt; tgt then {\r\n    item.answer as a -&gt;  tgt.extension as ext,  ext.url = 'https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/caregiver',  ext.valueReference = a &quot;DE47-1&quot;;\r\n  } &quot;DE47-main&quot;;\r\n  src.item as item where linkId = 'EmCare.A.DE31' -&gt; tgt then {\r\n    item.answer as a -&gt;  tgt.extension as ext,  ext.url = 'https://fhir.dk.swisstph-mis.ch/matchbox/fhir/Extension/motherVitalStatus',  ext.valueCode = a &quot;DE31-1&quot;;\r\n  } &quot;DE31-main&quot;;\r\n  src.item as item where linkId = 'EmCare.A.DE32' -&gt; tgt then {\r\n    item.answer as a -&gt;  tgt.extension as ext,  ext.url = 'https://fhir.dk.swisstph-mis.ch/matchbox/fhir/Extension/fatherVitalStatus',  ext.valueCode = a &quot;DE32-1&quot;;\r\n  } &quot;DE32-main&quot;;\r\n}\r\n\r\ngroup SetOfficalGivenNameemcareade04(source src, target tgt) {\r\n  src -&gt; tgt.use = 'official' then {\r\n    src.item as item where linkId = 'EmCare.A.DE04' then {\r\n      item.answer as a -&gt; tgt.given = a &quot;f&quot;;\r\n    } &quot;first&quot;;\r\n    src.item as item where linkId = 'EmCare.A.DE05' then {\r\n      item.answer as a -&gt; tgt.given = a &quot;m&quot;;\r\n    } &quot;middle&quot;;\r\n    src.item as item where linkId = 'EmCare.A.DE06' then {\r\n      item.answer as a -&gt; tgt.family = a &quot;fa&quot;;\r\n    } &quot;family&quot;;\r\n  } &quot;details&quot;;\r\n}\r\n\r\ngroup emcareencounteremcarearegistrationpgroup(source src : questionnaireResponse, target tgt : Encounter, source resid) {\r\n  src -&gt;  tgt.id = resid,  tgt.meta = create('Meta') as newMeta,  newMeta.profile = 'https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/encounter' &quot;set-uuid&quot;;\r\n  src.item as item where linkId = 'EmCare.A.DE07' -&gt; tgt then {\r\n    item.answer as a -&gt;  tgt.period as period,  period.start = a &quot;DE07-1&quot;;\r\n  } &quot;DE07-main&quot;;\r\n}\r\n\r\ngroup emcarerelatedpersoncaregiveremcarearegistrationpgroup(source src : questionnaireResponse, target tgt : RelatedPerson, source resid) {\r\n  src -&gt;  tgt.id = resid,  tgt.meta = create('Meta') as newMeta,  newMeta.profile = 'https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/relatedperson' &quot;set-uuid&quot;;\r\n  src.item first as item where (linkId = 'EmCare.A.DE21') or (linkId = 'EmCare.A.DE22') or (linkId = 'EmCare.A.DE23') -&gt;  tgt as target,  target.name as name then SetOfficalGivenNameemcareade21(src, name) &quot;name-main&quot;;\r\n  src.item as item where linkId = 'EmCare.A.DE35' -&gt; tgt then {\r\n    item.answer as a -&gt;  tgt.telecom as tel,  tel.system = 'phone',  tel.use = 'mobile',  tel.value = a &quot;DE35-1&quot;;\r\n  } &quot;DE35-main&quot;;\r\n  src.item as item where linkId = 'EmCare.A.DE36' -&gt; tgt then {\r\n    item.answer as a -&gt;  tgt.telecom as tel,  tel.system = 'phone',  tel.use = 'home',  tel.value = a &quot;DE36-1&quot;;\r\n  } &quot;DE36-main&quot;;\r\n  src.item as item where linkId = 'EmCare.A.DE37' -&gt; tgt then {\r\n    item.answer as a -&gt;  tgt.telecom as tel,  tel.system = 'phone',  tel.use = 'work',  tel.value = a &quot;DE37-1&quot;;\r\n  } &quot;DE37-main&quot;;\r\n}\r\n\r\ngroup SetOfficalGivenNameemcareade21(source src, target tgt) {\r\n  src -&gt; tgt.use = 'official' then {\r\n    src.item as item where linkId = 'EmCare.A.DE21' then {\r\n      item.answer as a -&gt; tgt.given = a &quot;f&quot;;\r\n    } &quot;first&quot;;\r\n    src.item as item where linkId = 'EmCare.A.DE22' then {\r\n      item.answer as a -&gt; tgt.given = a &quot;m&quot;;\r\n    } &quot;middle&quot;;\r\n    src.item as item where linkId = 'EmCare.A.DE23' then {\r\n      item.answer as a -&gt; tgt.family = a &quot;fa&quot;;\r\n    } &quot;family&quot;;\r\n  } &quot;details&quot;;\r\n}\r\n\r\ngroup emcarecommunicationrequestemcarearegistrationpgroup(source src : questionnaireResponse, target tgt : CommunicationRequest, source resid) {\r\n  src -&gt;  tgt.id = resid,  tgt.meta = create('Meta') as newMeta,  newMeta.profile = 'https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/communicationrequest' &quot;set-uuid&quot;;\r\n  src.item as item where linkId = 'EmCare.A.DE38' -&gt; tgt then {\r\n    item.answer as a -&gt; tgt.medium = 'https://fhir.dk.swisstph-mis.ch/matchbox/fhir/CodeSystem/emcare-custom-codes' &quot;DE38-1&quot;;\r\n  } &quot;DE38-main&quot;;\r\n  src.item as item where linkId = 'EmCare.A.DE46' -&gt; tgt then {\r\n    item.answer as a -&gt; tgt.recipient = a &quot;DE46-1&quot;;\r\n  } &quot;DE46-main&quot;;\r\n}\r\n\r\n</pre></div>"
  },
  "url": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureMap/emcarea.registration.p",
  "name": "emcarea.registration.p",
  "description": "emcarearegistrationp:https://fhir.dk.swisstph-mis.ch/matchbox/fhir/Questionnaire/emcarea.registration.p",
  "structure": [ {
    "url": "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaireresponse",
    "mode": "source",
    "alias": "'questionnaireResponse'",
    "documentation": "Bundle are require where there is multiple ressource to be mapped"
  }, {
    "url": "http://hl7.org/fhir/StructureDefinition/Bundle",
    "mode": "target",
    "alias": "'bundle'",
    "documentation": "target that will be inserted in the bundle"
  }, {
    "url": "http://hl7.org/fhir/StructureDefinition/Patient",
    "mode": "target",
    "alias": "'Patient'",
    "documentation": "target that will be inserted in the bundle"
  }, {
    "url": "http://hl7.org/fhir/StructureDefinition/Encounter",
    "mode": "target",
    "alias": "'Encounter'",
    "documentation": "target that will be inserted in the bundle"
  }, {
    "url": "http://hl7.org/fhir/StructureDefinition/RelatedPerson",
    "mode": "target",
    "alias": "'RelatedPerson'",
    "documentation": "target that will be inserted in the bundle"
  }, {
    "url": "http://hl7.org/fhir/StructureDefinition/CommunicationRequest",
    "mode": "target",
    "alias": "'CommunicationRequest'"
  } ],
  "group": [ {
    "name": "bundleMapping",
    "typeMode": "none",
    "input": [ {
      "name": "src",
      "type": "questionnaireResponse",
      "mode": "source"
    }, {
      "name": "bundle",
      "type": "Bundle",
      "mode": "target"
    } ],
    "rule": [ {
      "name": "id",
      "source": [ {
        "context": "src"
      } ],
      "target": [ {
        "context": "bundle",
        "contextType": "variable",
        "element": "id",
        "transform": "uuid"
      } ]
    }, {
      "name": "type",
      "source": [ {
        "context": "src"
      } ],
      "target": [ {
        "context": "bundle",
        "contextType": "variable",
        "element": "type",
        "transform": "copy",
        "parameter": [ {
          "valueString": "batch"
        } ]
      } ]
    }, {
      "name": "gen-ids",
      "source": [ {
        "context": "src"
      } ],
      "target": [ {
        "contextType": "variable",
        "variable": "emcarepatientemcarearegistrationpid",
        "transform": "uuid"
      }, {
        "contextType": "variable",
        "variable": "emcareencounteremcarearegistrationpid",
        "transform": "uuid"
      }, {
        "contextType": "variable",
        "variable": "emcarerelatedpersoncaregiveremcarearegistrationpid",
        "transform": "uuid"
      }, {
        "contextType": "variable",
        "variable": "emcarecommunicationrequestemcarearegistrationpid",
        "transform": "uuid"
      } ],
      "rule": [ {
        "name": "emcarepatientemcarearegistrationpgrouprule",
        "source": [ {
          "context": "src"
        } ],
        "target": [ {
          "context": "bundle",
          "contextType": "variable",
          "element": "entry",
          "variable": "entry"
        }, {
          "context": "entry",
          "contextType": "variable",
          "element": "fullUrl",
          "transform": "append",
          "parameter": [ {
            "valueString": "urn:uuid:"
          }, {
            "valueId": "emcarepatientemcarearegistrationpid"
          } ]
        }, {
          "context": "entry",
          "contextType": "variable",
          "element": "request",
          "variable": "request"
        }, {
          "context": "request",
          "contextType": "variable",
          "element": "method",
          "transform": "copy",
          "parameter": [ {
            "valueString": "PUT"
          } ]
        }, {
          "context": "request",
          "contextType": "variable",
          "element": "url",
          "transform": "append",
          "parameter": [ {
            "valueString": "Patient/"
          }, {
            "valueId": "emcarepatientemcarearegistrationpid"
          } ]
        }, {
          "context": "entry",
          "contextType": "variable",
          "element": "resource",
          "variable": "tgt",
          "transform": "create",
          "parameter": [ {
            "valueString": "Patient"
          } ]
        } ],
        "dependent": [ {
          "name": "emcarepatientemcarearegistrationpgroup",
          "variable": [ "src", "tgt", "emcarepatientemcarearegistrationpid" ]
        } ]
      }, {
        "name": "emcareencounteremcarearegistrationpgrouprule",
        "source": [ {
          "context": "src"
        } ],
        "target": [ {
          "context": "bundle",
          "contextType": "variable",
          "element": "entry",
          "variable": "entry"
        }, {
          "context": "entry",
          "contextType": "variable",
          "element": "fullUrl",
          "transform": "append",
          "parameter": [ {
            "valueString": "urn:uuid:"
          }, {
            "valueId": "emcareencounteremcarearegistrationpid"
          } ]
        }, {
          "context": "entry",
          "contextType": "variable",
          "element": "request",
          "variable": "request"
        }, {
          "context": "request",
          "contextType": "variable",
          "element": "method",
          "transform": "copy",
          "parameter": [ {
            "valueString": "PUT"
          } ]
        }, {
          "context": "request",
          "contextType": "variable",
          "element": "url",
          "transform": "append",
          "parameter": [ {
            "valueString": "Encounter/"
          }, {
            "valueId": "emcareencounteremcarearegistrationpid"
          } ]
        }, {
          "context": "entry",
          "contextType": "variable",
          "element": "resource",
          "variable": "tgt",
          "transform": "create",
          "parameter": [ {
            "valueString": "Encounter"
          } ]
        } ],
        "dependent": [ {
          "name": "emcareencounteremcarearegistrationpgroup",
          "variable": [ "src", "tgt", "emcareencounteremcarearegistrationpid" ]
        } ]
      }, {
        "name": "emcarerelatedpersoncaregiveremcarearegistrationpgrouprule",
        "source": [ {
          "context": "src"
        } ],
        "target": [ {
          "context": "bundle",
          "contextType": "variable",
          "element": "entry",
          "variable": "entry"
        }, {
          "context": "entry",
          "contextType": "variable",
          "element": "fullUrl",
          "transform": "append",
          "parameter": [ {
            "valueString": "urn:uuid:"
          }, {
            "valueId": "emcarerelatedpersoncaregiveremcarearegistrationpid"
          } ]
        }, {
          "context": "entry",
          "contextType": "variable",
          "element": "request",
          "variable": "request"
        }, {
          "context": "request",
          "contextType": "variable",
          "element": "method",
          "transform": "copy",
          "parameter": [ {
            "valueString": "PUT"
          } ]
        }, {
          "context": "request",
          "contextType": "variable",
          "element": "url",
          "transform": "append",
          "parameter": [ {
            "valueString": "RelatedPerson/"
          }, {
            "valueId": "emcarerelatedpersoncaregiveremcarearegistrationpid"
          } ]
        }, {
          "context": "entry",
          "contextType": "variable",
          "element": "resource",
          "variable": "tgt",
          "transform": "create",
          "parameter": [ {
            "valueString": "RelatedPerson"
          } ]
        } ],
        "dependent": [ {
          "name": "emcarerelatedpersoncaregiveremcarearegistrationpgroup",
          "variable": [ "src", "tgt", "emcarerelatedpersoncaregiveremcarearegistrationpid" ]
        } ]
      }, {
        "name": "emcarecommunicationrequestemcarearegistrationpgrouprule",
        "source": [ {
          "context": "src"
        } ],
        "target": [ {
          "context": "bundle",
          "contextType": "variable",
          "element": "entry",
          "variable": "entry"
        }, {
          "context": "entry",
          "contextType": "variable",
          "element": "fullUrl",
          "transform": "append",
          "parameter": [ {
            "valueString": "urn:uuid:"
          }, {
            "valueId": "emcarecommunicationrequestemcarearegistrationpid"
          } ]
        }, {
          "context": "entry",
          "contextType": "variable",
          "element": "request",
          "variable": "request"
        }, {
          "context": "request",
          "contextType": "variable",
          "element": "method",
          "transform": "copy",
          "parameter": [ {
            "valueString": "PUT"
          } ]
        }, {
          "context": "request",
          "contextType": "variable",
          "element": "url",
          "transform": "append",
          "parameter": [ {
            "valueString": "CommunicationRequest/"
          }, {
            "valueId": "emcarecommunicationrequestemcarearegistrationpid"
          } ]
        }, {
          "context": "entry",
          "contextType": "variable",
          "element": "resource",
          "variable": "tgt",
          "transform": "create",
          "parameter": [ {
            "valueString": "CommunicationRequest"
          } ]
        } ],
        "dependent": [ {
          "name": "emcarecommunicationrequestemcarearegistrationpgroup",
          "variable": [ "src", "tgt", "emcarecommunicationrequestemcarearegistrationpid" ]
        } ]
      } ]
    } ]
  }, {
    "name": "emcarepatientemcarearegistrationpgroup",
    "typeMode": "none",
    "input": [ {
      "name": "src",
      "type": "questionnaireResponse",
      "mode": "source"
    }, {
      "name": "tgt",
      "type": "Patient",
      "mode": "target"
    }, {
      "name": "resid",
      "mode": "source"
    } ],
    "rule": [ {
      "name": "set-uuid",
      "source": [ {
        "context": "src"
      } ],
      "target": [ {
        "context": "tgt",
        "contextType": "variable",
        "element": "id",
        "transform": "copy",
        "parameter": [ {
          "valueId": "resid"
        } ]
      }, {
        "context": "tgt",
        "contextType": "variable",
        "element": "meta",
        "variable": "newMeta",
        "transform": "create",
        "parameter": [ {
          "valueString": "Meta"
        } ]
      }, {
        "context": "newMeta",
        "contextType": "variable",
        "element": "profile",
        "transform": "copy",
        "parameter": [ {
          "valueString": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/patient"
        } ]
      } ]
    }, {
      "name": "EmCare.A.DE01-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "variable": "item",
        "condition": "linkId = 'EmCare.A.DE01'"
      } ],
      "target": [ {
        "contextType": "variable",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      } ],
      "rule": [ {
        "name": "set-id",
        "source": [ {
          "context": "item",
          "element": "answer",
          "variable": "a"
        } ],
        "target": [ {
          "context": "tgt",
          "contextType": "variable",
          "element": "identifier",
          "variable": "identifier",
          "transform": "create",
          "parameter": [ {
            "valueString": "Identifier"
          } ]
        } ],
        "rule": [ {
          "name": "id",
          "source": [ {
            "context": "a"
          } ],
          "target": [ {
            "context": "identifier",
            "contextType": "variable",
            "element": "value",
            "transform": "copy",
            "parameter": [ {
              "valueId": "a"
            } ]
          }, {
            "context": "identifier",
            "contextType": "variable",
            "element": "system",
            "transform": "copy",
            "parameter": [ {
              "valueString": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir//CodeSystem/em-custom"
            } ]
          } ]
        } ]
      } ]
    }, {
      "name": "EmCare.A.DE02-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "variable": "item",
        "condition": "linkId = 'EmCare.A.DE02'"
      } ],
      "target": [ {
        "contextType": "variable",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      } ],
      "rule": [ {
        "name": "set-id",
        "source": [ {
          "context": "item",
          "element": "answer",
          "variable": "a"
        } ],
        "target": [ {
          "context": "tgt",
          "contextType": "variable",
          "element": "identifier",
          "variable": "identifier",
          "transform": "create",
          "parameter": [ {
            "valueString": "Identifier"
          } ]
        } ],
        "rule": [ {
          "name": "idu",
          "source": [ {
            "context": "a"
          } ],
          "target": [ {
            "context": "identifier",
            "contextType": "variable",
            "element": "value",
            "transform": "copy",
            "parameter": [ {
              "valueId": "a"
            } ]
          }, {
            "context": "identifier",
            "contextType": "variable",
            "element": "system",
            "transform": "copy",
            "parameter": [ {
              "valueString": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir//CodeSystem/em-custom"
            } ]
          } ]
        } ]
      } ]
    }, {
      "name": "EmCare.A.DE03-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "variable": "item",
        "condition": "linkId = 'EmCare.A.DE03'"
      } ],
      "target": [ {
        "contextType": "variable",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      } ],
      "rule": [ {
        "name": "set-id",
        "source": [ {
          "context": "item",
          "element": "answer",
          "variable": "a"
        } ],
        "target": [ {
          "context": "tgt",
          "contextType": "variable",
          "element": "identifier",
          "variable": "identifier",
          "transform": "create",
          "parameter": [ {
            "valueString": "Identifier"
          } ]
        } ],
        "rule": [ {
          "name": "noid",
          "source": [ {
            "context": "a"
          } ],
          "target": [ {
            "context": "identifier",
            "contextType": "variable",
            "element": "value",
            "transform": "copy",
            "parameter": [ {
              "valueString": "noid"
            } ]
          }, {
            "context": "identifier",
            "contextType": "variable",
            "element": "system",
            "transform": "copy",
            "parameter": [ {
              "valueString": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir//CodeSystem/em-custom"
            } ]
          } ]
        } ]
      } ]
    }, {
      "name": "name-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "listMode": "first",
        "variable": "item",
        "condition": "(linkId = 'EmCare.A.DE04') or (linkId = 'EmCare.A.DE05') or (linkId = 'EmCare.A.DE06')"
      } ],
      "target": [ {
        "contextType": "variable",
        "variable": "target",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      }, {
        "context": "target",
        "contextType": "variable",
        "element": "name",
        "variable": "name"
      } ],
      "dependent": [ {
        "name": "SetOfficalGivenNameemcareade04",
        "variable": [ "src", "name" ]
      } ]
    }, {
      "name": "EmCare.A.DE08-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "variable": "item",
        "condition": "linkId = 'EmCare.A.DE08'"
      } ],
      "target": [ {
        "contextType": "variable",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      } ],
      "rule": [ {
        "name": "EmCare.A.DE08-1",
        "source": [ {
          "context": "item",
          "element": "answer",
          "variable": "a"
        } ],
        "target": [ {
          "context": "tgt",
          "contextType": "variable",
          "element": "birthDate",
          "transform": "copy",
          "parameter": [ {
            "valueId": "a"
          } ]
        } ]
      } ]
    }, {
      "name": "EmCare.A.DE12-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "variable": "item",
        "condition": "linkId = 'EmCare.A.DE12'"
      } ],
      "target": [ {
        "contextType": "variable",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      } ],
      "rule": [ {
        "name": "EmCare.A.DE12-1",
        "source": [ {
          "context": "item",
          "element": "answer",
          "variable": "a"
        } ],
        "target": [ {
          "context": "tgt",
          "contextType": "variable",
          "element": "extension",
          "variable": "ext"
        }, {
          "context": "ext",
          "contextType": "variable",
          "element": "url",
          "transform": "copy",
          "parameter": [ {
            "valueString": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/birthDateEstimator"
          } ]
        }, {
          "context": "ext",
          "contextType": "variable",
          "element": "valueCode",
          "transform": "copy",
          "parameter": [ {
            "valueId": "a"
          } ]
        } ]
      } ]
    }, {
      "name": "EmCare.A.DE16-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "variable": "item",
        "condition": "linkId = 'EmCare.A.DE16'"
      } ],
      "target": [ {
        "contextType": "variable",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      } ],
      "rule": [ {
        "name": "EmCare.A.DE16-1",
        "source": [ {
          "context": "item",
          "element": "answer",
          "variable": "a"
        } ],
        "target": [ {
          "context": "tgt",
          "contextType": "variable",
          "element": "gender",
          "transform": "translate",
          "parameter": [ {
            "valueId": "a"
          }, {
            "valueString": "sex-of-the-client"
          }, {
            "valueString": "http://hl7.org/fhir/administrative-gender"
          } ]
        } ]
      } ]
    }, {
      "name": "EmCare.A.DE20-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "variable": "item",
        "condition": "linkId = 'EmCare.A.DE20'"
      } ],
      "target": [ {
        "contextType": "variable",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      } ],
      "rule": [ {
        "name": "EmCare.A.DE20-1",
        "source": [ {
          "context": "item",
          "element": "answer",
          "variable": "a"
        } ],
        "target": [ {
          "context": "tgt",
          "contextType": "variable",
          "element": "address",
          "variable": "address"
        }, {
          "context": "address",
          "contextType": "variable",
          "element": "text",
          "transform": "copy",
          "parameter": [ {
            "valueId": "a"
          } ]
        } ]
      } ]
    }, {
      "name": "EmCare.A.DE47-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "variable": "item",
        "condition": "linkId = 'EmCare.A.DE47'"
      } ],
      "target": [ {
        "contextType": "variable",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      } ],
      "rule": [ {
        "name": "EmCare.A.DE47-1",
        "source": [ {
          "context": "item",
          "element": "answer",
          "variable": "a"
        } ],
        "target": [ {
          "context": "tgt",
          "contextType": "variable",
          "element": "extension",
          "variable": "ext"
        }, {
          "context": "ext",
          "contextType": "variable",
          "element": "url",
          "transform": "copy",
          "parameter": [ {
            "valueString": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/caregiver"
          } ]
        }, {
          "context": "ext",
          "contextType": "variable",
          "element": "valueReference",
          "transform": "copy",
          "parameter": [ {
            "valueId": "a"
          } ]
        } ]
      } ]
    }, {
      "name": "EmCare.A.DE31-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "variable": "item",
        "condition": "linkId = 'EmCare.A.DE31'"
      } ],
      "target": [ {
        "contextType": "variable",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      } ],
      "rule": [ {
        "name": "EmCare.A.DE31-1",
        "source": [ {
          "context": "item",
          "element": "answer",
          "variable": "a"
        } ],
        "target": [ {
          "context": "tgt",
          "contextType": "variable",
          "element": "extension",
          "variable": "ext"
        }, {
          "context": "ext",
          "contextType": "variable",
          "element": "url",
          "transform": "copy",
          "parameter": [ {
            "valueString": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/Extension/motherVitalStatus"
          } ]
        }, {
          "context": "ext",
          "contextType": "variable",
          "element": "valueCode",
          "transform": "copy",
          "parameter": [ {
            "valueId": "a"
          } ]
        } ]
      } ]
    }, {
      "name": "EmCare.A.DE32-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "variable": "item",
        "condition": "linkId = 'EmCare.A.DE32'"
      } ],
      "target": [ {
        "contextType": "variable",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      } ],
      "rule": [ {
        "name": "EmCare.A.DE32-1",
        "source": [ {
          "context": "item",
          "element": "answer",
          "variable": "a"
        } ],
        "target": [ {
          "context": "tgt",
          "contextType": "variable",
          "element": "extension",
          "variable": "ext"
        }, {
          "context": "ext",
          "contextType": "variable",
          "element": "url",
          "transform": "copy",
          "parameter": [ {
            "valueString": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/Extension/fatherVitalStatus"
          } ]
        }, {
          "context": "ext",
          "contextType": "variable",
          "element": "valueCode",
          "transform": "copy",
          "parameter": [ {
            "valueId": "a"
          } ]
        } ]
      } ]
    } ]
  }, {
    "name": "SetOfficalGivenNameemcareade04",
    "typeMode": "none",
    "input": [ {
      "name": "src",
      "mode": "source"
    }, {
      "name": "tgt",
      "mode": "target"
    } ],
    "rule": [ {
      "name": "details",
      "source": [ {
        "context": "src"
      } ],
      "target": [ {
        "context": "tgt",
        "contextType": "variable",
        "element": "use",
        "transform": "copy",
        "parameter": [ {
          "valueString": "official"
        } ]
      } ],
      "rule": [ {
        "name": "first",
        "source": [ {
          "context": "src",
          "element": "item",
          "variable": "item",
          "condition": "linkId = 'EmCare.A.DE04'"
        } ],
        "rule": [ {
          "name": "f",
          "source": [ {
            "context": "item",
            "element": "answer",
            "variable": "a"
          } ],
          "target": [ {
            "context": "tgt",
            "contextType": "variable",
            "element": "given",
            "transform": "copy",
            "parameter": [ {
              "valueId": "a"
            } ]
          } ]
        } ]
      }, {
        "name": "middle",
        "source": [ {
          "context": "src",
          "element": "item",
          "variable": "item",
          "condition": "linkId = 'EmCare.A.DE05'"
        } ],
        "rule": [ {
          "name": "m",
          "source": [ {
            "context": "item",
            "element": "answer",
            "variable": "a"
          } ],
          "target": [ {
            "context": "tgt",
            "contextType": "variable",
            "element": "given",
            "transform": "copy",
            "parameter": [ {
              "valueId": "a"
            } ]
          } ]
        } ]
      }, {
        "name": "family",
        "source": [ {
          "context": "src",
          "element": "item",
          "variable": "item",
          "condition": "linkId = 'EmCare.A.DE06'"
        } ],
        "rule": [ {
          "name": "fa",
          "source": [ {
            "context": "item",
            "element": "answer",
            "variable": "a"
          } ],
          "target": [ {
            "context": "tgt",
            "contextType": "variable",
            "element": "family",
            "transform": "copy",
            "parameter": [ {
              "valueId": "a"
            } ]
          } ]
        } ]
      } ]
    } ]
  }, {
    "name": "emcareencounteremcarearegistrationpgroup",
    "typeMode": "none",
    "input": [ {
      "name": "src",
      "type": "questionnaireResponse",
      "mode": "source"
    }, {
      "name": "tgt",
      "type": "Encounter",
      "mode": "target"
    }, {
      "name": "resid",
      "mode": "source"
    } ],
    "rule": [ {
      "name": "set-uuid",
      "source": [ {
        "context": "src"
      } ],
      "target": [ {
        "context": "tgt",
        "contextType": "variable",
        "element": "id",
        "transform": "copy",
        "parameter": [ {
          "valueId": "resid"
        } ]
      }, {
        "context": "tgt",
        "contextType": "variable",
        "element": "meta",
        "variable": "newMeta",
        "transform": "create",
        "parameter": [ {
          "valueString": "Meta"
        } ]
      }, {
        "context": "newMeta",
        "contextType": "variable",
        "element": "profile",
        "transform": "copy",
        "parameter": [ {
          "valueString": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/encounter"
        } ]
      } ]
    }, {
      "name": "EmCare.A.DE07-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "variable": "item",
        "condition": "linkId = 'EmCare.A.DE07'"
      } ],
      "target": [ {
        "contextType": "variable",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      } ],
      "rule": [ {
        "name": "EmCare.A.DE07-1",
        "source": [ {
          "context": "item",
          "element": "answer",
          "variable": "a"
        } ],
        "target": [ {
          "context": "tgt",
          "contextType": "variable",
          "element": "period",
          "variable": "period"
        }, {
          "context": "period",
          "contextType": "variable",
          "element": "start",
          "transform": "copy",
          "parameter": [ {
            "valueId": "a"
          } ]
        } ]
      } ]
    } ]
  }, {
    "name": "emcarerelatedpersoncaregiveremcarearegistrationpgroup",
    "typeMode": "none",
    "input": [ {
      "name": "src",
      "type": "questionnaireResponse",
      "mode": "source"
    }, {
      "name": "tgt",
      "type": "RelatedPerson",
      "mode": "target"
    }, {
      "name": "resid",
      "mode": "source"
    } ],
    "rule": [ {
      "name": "set-uuid",
      "source": [ {
        "context": "src"
      } ],
      "target": [ {
        "context": "tgt",
        "contextType": "variable",
        "element": "id",
        "transform": "copy",
        "parameter": [ {
          "valueId": "resid"
        } ]
      }, {
        "context": "tgt",
        "contextType": "variable",
        "element": "meta",
        "variable": "newMeta",
        "transform": "create",
        "parameter": [ {
          "valueString": "Meta"
        } ]
      }, {
        "context": "newMeta",
        "contextType": "variable",
        "element": "profile",
        "transform": "copy",
        "parameter": [ {
          "valueString": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/relatedperson"
        } ]
      } ]
    }, {
      "name": "name-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "listMode": "first",
        "variable": "item",
        "condition": "(linkId = 'EmCare.A.DE21') or (linkId = 'EmCare.A.DE22') or (linkId = 'EmCare.A.DE23')"
      } ],
      "target": [ {
        "contextType": "variable",
        "variable": "target",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      }, {
        "context": "target",
        "contextType": "variable",
        "element": "name",
        "variable": "name"
      } ],
      "dependent": [ {
        "name": "SetOfficalGivenNameemcareade21",
        "variable": [ "src", "name" ]
      } ]
    }, {
      "name": "EmCare.A.DE35-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "variable": "item",
        "condition": "linkId = 'EmCare.A.DE35'"
      } ],
      "target": [ {
        "contextType": "variable",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      } ],
      "rule": [ {
        "name": "EmCare.A.DE35-1",
        "source": [ {
          "context": "item",
          "element": "answer",
          "variable": "a"
        } ],
        "target": [ {
          "context": "tgt",
          "contextType": "variable",
          "element": "telecom",
          "variable": "tel"
        }, {
          "context": "tel",
          "contextType": "variable",
          "element": "system",
          "transform": "copy",
          "parameter": [ {
            "valueString": "phone"
          } ]
        }, {
          "context": "tel",
          "contextType": "variable",
          "element": "use",
          "transform": "copy",
          "parameter": [ {
            "valueString": "mobile"
          } ]
        }, {
          "context": "tel",
          "contextType": "variable",
          "element": "value",
          "transform": "copy",
          "parameter": [ {
            "valueId": "a"
          } ]
        } ]
      } ]
    }, {
      "name": "EmCare.A.DE36-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "variable": "item",
        "condition": "linkId = 'EmCare.A.DE36'"
      } ],
      "target": [ {
        "contextType": "variable",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      } ],
      "rule": [ {
        "name": "EmCare.A.DE36-1",
        "source": [ {
          "context": "item",
          "element": "answer",
          "variable": "a"
        } ],
        "target": [ {
          "context": "tgt",
          "contextType": "variable",
          "element": "telecom",
          "variable": "tel"
        }, {
          "context": "tel",
          "contextType": "variable",
          "element": "system",
          "transform": "copy",
          "parameter": [ {
            "valueString": "phone"
          } ]
        }, {
          "context": "tel",
          "contextType": "variable",
          "element": "use",
          "transform": "copy",
          "parameter": [ {
            "valueString": "home"
          } ]
        }, {
          "context": "tel",
          "contextType": "variable",
          "element": "value",
          "transform": "copy",
          "parameter": [ {
            "valueId": "a"
          } ]
        } ]
      } ]
    }, {
      "name": "EmCare.A.DE37-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "variable": "item",
        "condition": "linkId = 'EmCare.A.DE37'"
      } ],
      "target": [ {
        "contextType": "variable",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      } ],
      "rule": [ {
        "name": "EmCare.A.DE37-1",
        "source": [ {
          "context": "item",
          "element": "answer",
          "variable": "a"
        } ],
        "target": [ {
          "context": "tgt",
          "contextType": "variable",
          "element": "telecom",
          "variable": "tel"
        }, {
          "context": "tel",
          "contextType": "variable",
          "element": "system",
          "transform": "copy",
          "parameter": [ {
            "valueString": "phone"
          } ]
        }, {
          "context": "tel",
          "contextType": "variable",
          "element": "use",
          "transform": "copy",
          "parameter": [ {
            "valueString": "work"
          } ]
        }, {
          "context": "tel",
          "contextType": "variable",
          "element": "value",
          "transform": "copy",
          "parameter": [ {
            "valueId": "a"
          } ]
        } ]
      } ]
    } ]
  }, {
    "name": "SetOfficalGivenNameemcareade21",
    "typeMode": "none",
    "input": [ {
      "name": "src",
      "mode": "source"
    }, {
      "name": "tgt",
      "mode": "target"
    } ],
    "rule": [ {
      "name": "details",
      "source": [ {
        "context": "src"
      } ],
      "target": [ {
        "context": "tgt",
        "contextType": "variable",
        "element": "use",
        "transform": "copy",
        "parameter": [ {
          "valueString": "official"
        } ]
      } ],
      "rule": [ {
        "name": "first",
        "source": [ {
          "context": "src",
          "element": "item",
          "variable": "item",
          "condition": "linkId = 'EmCare.A.DE21'"
        } ],
        "rule": [ {
          "name": "f",
          "source": [ {
            "context": "item",
            "element": "answer",
            "variable": "a"
          } ],
          "target": [ {
            "context": "tgt",
            "contextType": "variable",
            "element": "given",
            "transform": "copy",
            "parameter": [ {
              "valueId": "a"
            } ]
          } ]
        } ]
      }, {
        "name": "middle",
        "source": [ {
          "context": "src",
          "element": "item",
          "variable": "item",
          "condition": "linkId = 'EmCare.A.DE22'"
        } ],
        "rule": [ {
          "name": "m",
          "source": [ {
            "context": "item",
            "element": "answer",
            "variable": "a"
          } ],
          "target": [ {
            "context": "tgt",
            "contextType": "variable",
            "element": "given",
            "transform": "copy",
            "parameter": [ {
              "valueId": "a"
            } ]
          } ]
        } ]
      }, {
        "name": "family",
        "source": [ {
          "context": "src",
          "element": "item",
          "variable": "item",
          "condition": "linkId = 'EmCare.A.DE23'"
        } ],
        "rule": [ {
          "name": "fa",
          "source": [ {
            "context": "item",
            "element": "answer",
            "variable": "a"
          } ],
          "target": [ {
            "context": "tgt",
            "contextType": "variable",
            "element": "family",
            "transform": "copy",
            "parameter": [ {
              "valueId": "a"
            } ]
          } ]
        } ]
      } ]
    } ]
  }, {
    "name": "emcarecommunicationrequestemcarearegistrationpgroup",
    "typeMode": "none",
    "input": [ {
      "name": "src",
      "type": "questionnaireResponse",
      "mode": "source"
    }, {
      "name": "tgt",
      "type": "CommunicationRequest",
      "mode": "target"
    }, {
      "name": "resid",
      "mode": "source"
    } ],
    "rule": [ {
      "name": "set-uuid",
      "source": [ {
        "context": "src"
      } ],
      "target": [ {
        "context": "tgt",
        "contextType": "variable",
        "element": "id",
        "transform": "copy",
        "parameter": [ {
          "valueId": "resid"
        } ]
      }, {
        "context": "tgt",
        "contextType": "variable",
        "element": "meta",
        "variable": "newMeta",
        "transform": "create",
        "parameter": [ {
          "valueString": "Meta"
        } ]
      }, {
        "context": "newMeta",
        "contextType": "variable",
        "element": "profile",
        "transform": "copy",
        "parameter": [ {
          "valueString": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/communicationrequest"
        } ]
      } ]
    }, {
      "name": "EmCare.A.DE38-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "variable": "item",
        "condition": "linkId = 'EmCare.A.DE38'"
      } ],
      "target": [ {
        "contextType": "variable",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      } ],
      "rule": [ {
        "name": "EmCare.A.DE38-1",
        "source": [ {
          "context": "item",
          "element": "answer",
          "variable": "a"
        } ],
        "target": [ {
          "context": "tgt",
          "contextType": "variable",
          "element": "medium",
          "transform": "copy",
          "parameter": [ {
            "valueString": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/CodeSystem/emcare-custom-codes"
          } ]
        } ]
      } ]
    }, {
      "name": "EmCare.A.DE46-main",
      "source": [ {
        "context": "src",
        "element": "item",
        "variable": "item",
        "condition": "linkId = 'EmCare.A.DE46'"
      } ],
      "target": [ {
        "contextType": "variable",
        "transform": "copy",
        "parameter": [ {
          "valueId": "tgt"
        } ]
      } ],
      "rule": [ {
        "name": "EmCare.A.DE46-1",
        "source": [ {
          "context": "item",
          "element": "answer",
          "variable": "a"
        } ],
        "target": [ {
          "context": "tgt",
          "contextType": "variable",
          "element": "recipient",
          "transform": "copy",
          "parameter": [ {
            "valueId": "a"
          } ]
        } ]
      } ]
    } ]
  } ]
}"""
      val structureMap = FhirContext.forR4().newJsonParser().parseResource(structureMapString) as StructureMap
      val entry = ResourceMapper.extract(
        questionnaireResource,
        questionnaireResponse,
        StructureMapExtractionContext(context = getApplication()) { _, _ -> structureMap
        }
      ).entryFirstRep

      print("The Extracted Resource:")
      print(FhirContext.forR4().newJsonParser().encodeResourceToString(entry.resource))
    }
  }

  private fun getQuestionnaireJson(): String {
    questionnaireJson?.let {
      return it!!
    }
    questionnaireJson = readFileFromAssets(state[AddPatientFragment.QUESTIONNAIRE_FILE_PATH_KEY]!!)
    return questionnaireJson!!
  }

  private fun readFileFromAssets(filename: String): String {
    return getApplication<Application>().assets.open(filename).bufferedReader().use {
      it.readText()
    }
  }

  private fun generateUuid(): String {
    return UUID.randomUUID().toString()
  }
}
