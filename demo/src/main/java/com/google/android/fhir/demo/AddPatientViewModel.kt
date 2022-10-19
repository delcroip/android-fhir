/*
 * Copyright 2022 Google LLC
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
import com.google.android.fhir.datacapture.common.datatype.asStringValue
import com.google.android.fhir.datacapture.createQuestionnaireResponseItem
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.mapping.StructureMapExtractionContext
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import java.util.UUID
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*
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
  var questionnaireResponse: String? = null

  /**
   * Saves patient registration questionnaire response into the application database.
   *
   * @param questionnaireResponse patient registration questionnaire response
   */
  fun savePatient(questionnaireResponse: QuestionnaireResponse) {
    viewModelScope.launch {
      val structureMapString = """{
                "resourceType": "StructureMap",
                "id": "emcareb.registration.e",
                "text": {
                    "status": "generated",
                    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><pre>map &quot;https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureMap/emcareb.registration.e&quot; = &quot;emcareb.registration.e&quot;\r\n\r\n\r\nuses &quot;http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaireresponse&quot; alias 'questionnaireResponse' as source\r\nuses &quot;http://hl7.org/fhir/StructureDefinition/Bundle&quot; alias 'Bundle' as target\r\nuses &quot;https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/encounter&quot; alias 'Encounter' as target\r\nuses &quot;https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/relatedperson&quot; alias 'RelatedPerson' as target\r\nuses &quot;https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/observation&quot; alias 'Observation' as target\r\nuses &quot;https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/emcareencounter&quot; alias 'EmCare Encounter' as produced\r\nuses &quot;https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/emcarerelatedperson&quot; alias 'EmCare RelatedPerson' as produced\r\nuses &quot;https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/emcareobservation&quot; alias 'EmCare Observation' as produced\r\n\r\ngroup bundletrans(source src : questionnaireResponse, target bundle : Bundle) {\r\n  src -&gt; bundle.id = uuid() &quot;id&quot;;\r\n  src -&gt; bundle.type = 'batch' &quot;type&quot;;\r\n  src -&gt; bundle.entry as entry then {\r\n    src.encounter as encounter then {\r\n      encounter.id as idval -&gt;  entry.request as request,  request.method = 'PUT',  request.url = append('/Encounter/', idval) &quot;f7ed42d5&quot;;\r\n    } &quot;49f4ed45&quot;;\r\n    src -&gt; entry.resource = create('Encounter') as tgt then {\r\n      src -&gt; tgt then emcareencounter(src, tgt) &quot;fd415ba8&quot;;\r\n      src.subject as sub -&gt; tgt.subject = sub &quot;23252e10&quot;;\r\n    } &quot;aa65c498&quot;;\r\n  } &quot;put-emcareencounter&quot;;\r\n  src where src.item.where(linkId = 'emcarerelatedpersonid').answer.exists() -&gt; bundle.entry as entry then {\r\n    src.item first as item where linkId = 'emcarerelatedpersonid' -&gt;  entry.request as request,  request.method = 'PUT' then {\r\n      item.answer first as a -&gt; request then {\r\n        a.value as val -&gt; request.url = append('/RelatedPerson/', val) &quot;11e3b388&quot;;\r\n      } &quot;c5e7331f&quot;;\r\n    } &quot;0e46d0c4&quot;;\r\n    src -&gt; entry.resource = create('RelatedPerson') as tgt then {\r\n      src -&gt; tgt then emcarerelatedperson(src, tgt) &quot;ec9147dd&quot;;\r\n    } &quot;3aa4d4d9&quot;;\r\n  } &quot;put-emcarerelatedperson&quot;;\r\n  src where src.item.where(linkId = 'EmCare.B3.DE05').exists() then {\r\n    src -&gt;  bundle.entry as entry,  entry.request as request,  request.method = 'POST',  entry.resource = create('Observation') as tgt then emcareobservationemcareb3de05(src, tgt) &quot;DE05&quot;;\r\n  } &quot;emcareobservation&quot;;\r\n}\r\n\r\ngroup emcareencounter(source src : questionnaireResponse, target tgt : Encounter) {\r\n  src.item as item where linkId = 'EmCare.A.DE07' then {\r\n    item.answer first as a then {\r\n      a.value as val -&gt;  tgt.period as period,  period.start = val &quot;aemcareade07&quot;;\r\n    } &quot;aemcareade07&quot;;\r\n  } &quot;emcareade07&quot;;\r\n  src.item as item where linkId = 'EmCare.B2.DE01' then {\r\n    item.answer first as a then {\r\n      a.value as val -&gt;  tgt.type = create('CodeableConcept') as CC,  CC.text = 'type of visit',  CC.coding = val &quot;aemcareb2de01&quot;;\r\n    } &quot;aemcareb2de01&quot;;\r\n  } &quot;emcareb2de01&quot;;\r\n  src.item as item where linkId = 'EmCare.B3.DE01' then {\r\n    item.answer first as a then {\r\n      a.value as val -&gt;  tgt.reasonCode = create('CodeableConcept') as CC,  CC.text = 'new consultation',  CC.coding = val &quot;aemcareb3de01&quot;;\r\n    } &quot;aemcareb3de01&quot;;\r\n  } &quot;emcareb3de01&quot;;\r\n  src.item as item where linkId = 'EmCare.B3.DE06' then {\r\n    item.answer first as a then {\r\n      a.value as val -&gt;  tgt.type = create('CodeableConcept') as CC,  CC.text = 'new consultation',  CC.coding = val &quot;aemcareb3de06&quot;;\r\n    } &quot;aemcareb3de06&quot;;\r\n  } &quot;emcareb3de06&quot;;\r\n  src.item as item where linkId = 'EmCare.B3.DE09' then {\r\n    item.answer first as a then {\r\n      a.value as val -&gt;  tgt.extension = create('Extension') as ext,  ext.url = 'https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/refered',  ext.value = val &quot;aemcareb3de09&quot;;\r\n    } &quot;aemcareb3de09&quot;;\r\n  } &quot;emcareb3de09&quot;;\r\n}\r\n\r\ngroup getIdemcareencounter(source src, target tgt) {\r\n  src.item first as item where linkId = 'emcareencounterid' -&gt; tgt then {\r\n    item.answer first as a -&gt; tgt then {\r\n      a.value as val -&gt; tgt.id = val &quot;8e3eaf71&quot;;\r\n    } &quot;f3550c0d&quot;;\r\n  } &quot;269afe68&quot;;\r\n}\r\n\r\ngroup getFullUrlemcareencounter(source src, target tgt) {\r\n  src.item first as item where linkId = 'emcareencounterid' -&gt; tgt then {\r\n    item.answer first as a -&gt; tgt then {\r\n      a.value as val -&gt; tgt.fullUrl = append('urn:uuid:', val) &quot;a6735326&quot;;\r\n    } &quot;8a0f6df2&quot;;\r\n  } &quot;d383794d&quot;;\r\n}\r\n\r\ngroup getUrlemcareencounter(source src, target tgt) {\r\n  src.item first as item where linkId = 'emcareencounterid' -&gt; tgt then {\r\n    item.answer first as a -&gt; tgt then {\r\n      a.value as val -&gt; ref.reference = append('/Encounter/', val) &quot;e8494603&quot;;\r\n    } &quot;35ea1ad7&quot;;\r\n  } &quot;558d60eb&quot;;\r\n}\r\n\r\ngroup emcarerelatedperson(source src : questionnaireResponse, target tgt : RelatedPerson) {\r\n  src.item first as item where (linkId = 'EmCare.A.DE40') or (linkId = 'EXXXXXXX') or (linkId = 'EmCare.A.DE40') -&gt;  tgt as target,  target.name as name then SetOfficalGivenNameemcarerelatedperson(src, name) &quot;emcareade40&quot;;\r\n}\r\n\r\ngroup SetOfficalGivenNameemcarerelatedperson(source src, target tgt) {\r\n  src -&gt; tgt.use = 'official' then {\r\n    src.item as item where linkId = 'EmCare.A.DE40' then {\r\n      item.answer first as a then {\r\n        a.value as val -&gt; tgt.given = val &quot;577eab5d&quot;;\r\n      } &quot;dcd99d74&quot;;\r\n    } &quot;3674e72e&quot;;\r\n    src.item as item where linkId = 'EXXXXXXX' then {\r\n      item.answer first as a then {\r\n        a.value as val -&gt; tgt.given = val &quot;577eab5d&quot;;\r\n      } &quot;dcd99d74&quot;;\r\n    } &quot;901b64d6&quot;;\r\n    src.item as item where linkId = 'EmCare.A.DE40' then {\r\n      item.answer first as a then {\r\n        a.value as val -&gt; tgt.family = val &quot;2374a9a1&quot;;\r\n      } &quot;09801739&quot;;\r\n    } &quot;512ccb78&quot;;\r\n  } &quot;eb5b3f08&quot;;\r\n}\r\n\r\ngroup getIdemcarerelatedperson(source src, target tgt) {\r\n  src.item first as item where linkId = 'emcarerelatedpersonid' -&gt; tgt then {\r\n    item.answer first as a -&gt; tgt then {\r\n      a.value as val -&gt; tgt.id = val &quot;8e3eaf71&quot;;\r\n    } &quot;f3550c0d&quot;;\r\n  } &quot;de9cc039&quot;;\r\n}\r\n\r\ngroup getFullUrlemcarerelatedperson(source src, target tgt) {\r\n  src.item first as item where linkId = 'emcarerelatedpersonid' -&gt; tgt then {\r\n    item.answer first as a -&gt; tgt then {\r\n      a.value as val -&gt; tgt.fullUrl = append('urn:uuid:', val) &quot;a6735326&quot;;\r\n    } &quot;8a0f6df2&quot;;\r\n  } &quot;00c7b89d&quot;;\r\n}\r\n\r\ngroup getUrlemcarerelatedperson(source src, target tgt) {\r\n  src.item first as item where linkId = 'emcarerelatedpersonid' -&gt; tgt then {\r\n    item.answer first as a -&gt; tgt then {\r\n      a.value as val -&gt; ref.reference = append('/RelatedPerson/', val) &quot;25b16148&quot;;\r\n    } &quot;00b2016a&quot;;\r\n  } &quot;ce147194&quot;;\r\n}\r\n\r\ngroup emcareobservationemcareb3de05(source src, target tgt) {\r\n  src -&gt;  tgt.identifier = create('Identifier') as CodeID,  CodeID.system = 'http://hl7.org/fhir/namingsystem-identifier-type',  CodeID.use = 'official',  CodeID.value = 'uuid',  CodeID.id = uuid() &quot;id-emcareb3de05&quot;;\r\n  src -&gt;  tgt.encounter = src.encounter,  tgt.subject = src.subject,  tgt.meta = create('Meta') as newMeta,  newMeta.profile = 'https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/emcareobservation',  tgt.code = create('CodeableConcept') as concept,  concept.system = 'https://fhir.dk.swisstph-mis.ch/matchbox/fhir/CodeSystem/emcare-custom-codes',  concept.code = 'EmCare.B3.DE05' &quot;code-emcareb3de05&quot;;\r\n  src.item as item where linkId = 'timestamp', item.answer as a -&gt; tgt.issued = a &quot;timestamp-emcareb3de05&quot;;\r\n  src -&gt; tgt.subject = src.subject &quot;patient&quot;;\r\n  src.item first as item where linkId = 'EmCare.B3.DE05' then {\r\n    item.answer first as a then {\r\n      a where a.value = 'yes' -&gt; tgt.status = 'final' &quot;final-emcareb3de05&quot;;\r\n      a where a.value = 'no' -&gt; tgt.status = 'cancelled' &quot;notfound-emcareb3de05&quot;;\r\n    } &quot;an-emcareb3de05&quot;;\r\n  } &quot;it-emcareb3de05&quot;;\r\n}\r\n\r\n</pre></div>"
                },
                "url": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureMap/emcareb.registration.e",
                "name": "emcareb.registration.e",
                "status": "active",
                "structure": [
                    {
                        "url": "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaireresponse",
                        "mode": "source",
                        "alias": "'questionnaireResponse'"
                    },
                    {
                        "url": "http://hl7.org/fhir/StructureDefinition/Bundle",
                        "mode": "target",
                        "alias": "'Bundle'"
                    },
                    {
                        "url": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/encounter",
                        "mode": "target",
                        "alias": "'Encounter'"
                    },
                    {
                        "url": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/relatedperson",
                        "mode": "target",
                        "alias": "'RelatedPerson'"
                    },
                    {
                        "url": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/observation",
                        "mode": "target",
                        "alias": "'Observation'"
                    },
                    {
                        "url": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/emcareencounter",
                        "mode": "produced",
                        "alias": "'EmCare Encounter'"
                    },
                    {
                        "url": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/emcarerelatedperson",
                        "mode": "produced",
                        "alias": "'EmCare RelatedPerson'"
                    },
                    {
                        "url": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/emcareobservation",
                        "mode": "produced",
                        "alias": "'EmCare Observation'"
                    }
                ],
                "group": [
                    {
                        "name": "bundletrans",
                        "typeMode": "none",
                        "input": [
                            {
                                "name": "src",
                                "type": "questionnaireResponse",
                                "mode": "source"
                            },
                            {
                                "name": "bundle",
                                "type": "Bundle",
                                "mode": "target"
                            }
                        ],
                        "rule": [
                            {
                                "name": "id",
                                "source": [
                                    {
                                        "context": "src"
                                    }
                                ],
                                "target": [
                                    {
                                        "context": "bundle",
                                        "contextType": "variable",
                                        "element": "id",
                                        "transform": "uuid"
                                    }
                                ]
                            },
                            {
                                "name": "type",
                                "source": [
                                    {
                                        "context": "src"
                                    }
                                ],
                                "target": [
                                    {
                                        "context": "bundle",
                                        "contextType": "variable",
                                        "element": "type",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueString": "batch"
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "name": "put-emcareencounter",
                                "source": [
                                    {
                                        "context": "src"
                                    }
                                ],
                                "target": [
                                    {
                                        "context": "bundle",
                                        "contextType": "variable",
                                        "element": "entry",
                                        "variable": "entry"
                                    }
                                ],
                                "rule": [
                                    {
                                        "name": "49f4ed45",
                                        "source": [
                                            {
                                                "context": "src",
                                                "element": "encounter",
                                                "variable": "encounter"
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "f7ed42d5",
                                                "source": [
                                                    {
                                                        "context": "encounter",
                                                        "element": "id",
                                                        "variable": "idval"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "context": "entry",
                                                        "contextType": "variable",
                                                        "element": "request",
                                                        "variable": "request"
                                                    },
                                                    {
                                                        "context": "request",
                                                        "contextType": "variable",
                                                        "element": "method",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueString": "PUT"
                                                            }
                                                        ]
                                                    },
                                                    {
                                                        "context": "request",
                                                        "contextType": "variable",
                                                        "element": "url",
                                                        "transform": "append",
                                                        "parameter": [
                                                            {
                                                                "valueString": "/Encounter/"
                                                            },
                                                            {
                                                                "valueId": "idval"
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    },
                                    {
                                        "name": "aa65c498",
                                        "source": [
                                            {
                                                "context": "src"
                                            }
                                        ],
                                        "target": [
                                            {
                                                "context": "entry",
                                                "contextType": "variable",
                                                "element": "resource",
                                                "variable": "tgt",
                                                "transform": "create",
                                                "parameter": [
                                                    {
                                                        "valueString": "Encounter"
                                                    }
                                                ]
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "fd415ba8",
                                                "source": [
                                                    {
                                                        "context": "src"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "contextType": "variable",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueId": "tgt"
                                                            }
                                                        ]
                                                    }
                                                ],
                                                "dependent": [
                                                    {
                                                        "name": "emcareencounter",
                                                        "variable": [
                                                            "src",
                                                            "tgt"
                                                        ]
                                                    }
                                                ]
                                            },
                                            {
                                                "name": "23252e10",
                                                "source": [
                                                    {
                                                        "context": "src",
                                                        "element": "subject",
                                                        "variable": "sub"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "context": "tgt",
                                                        "contextType": "variable",
                                                        "element": "subject",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueId": "sub"
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "name": "put-emcarerelatedperson",
                                "source": [
                                    {
                                        "context": "src",
                                        "condition": "src.item.where(linkId = 'emcarerelatedpersonid').answer.exists()"
                                    }
                                ],
                                "target": [
                                    {
                                        "context": "bundle",
                                        "contextType": "variable",
                                        "element": "entry",
                                        "variable": "entry"
                                    }
                                ],
                                "rule": [
                                    {
                                        "name": "0e46d0c4",
                                        "source": [
                                            {
                                                "context": "src",
                                                "element": "item",
                                                "listMode": "first",
                                                "variable": "item",
                                                "condition": "linkId = 'emcarerelatedpersonid'"
                                            }
                                        ],
                                        "target": [
                                            {
                                                "context": "entry",
                                                "contextType": "variable",
                                                "element": "request",
                                                "variable": "request"
                                            },
                                            {
                                                "context": "request",
                                                "contextType": "variable",
                                                "element": "method",
                                                "transform": "copy",
                                                "parameter": [
                                                    {
                                                        "valueString": "PUT"
                                                    }
                                                ]
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "c5e7331f",
                                                "source": [
                                                    {
                                                        "context": "item",
                                                        "element": "answer",
                                                        "listMode": "first",
                                                        "variable": "a"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "contextType": "variable",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueId": "request"
                                                            }
                                                        ]
                                                    }
                                                ],
                                                "rule": [
                                                    {
                                                        "name": "11e3b388",
                                                        "source": [
                                                            {
                                                                "context": "a",
                                                                "element": "value",
                                                                "variable": "val"
                                                            }
                                                        ],
                                                        "target": [
                                                            {
                                                                "context": "request",
                                                                "contextType": "variable",
                                                                "element": "url",
                                                                "transform": "append",
                                                                "parameter": [
                                                                    {
                                                                        "valueString": "/RelatedPerson/"
                                                                    },
                                                                    {
                                                                        "valueId": "val"
                                                                    }
                                                                ]
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    },
                                    {
                                        "name": "3aa4d4d9",
                                        "source": [
                                            {
                                                "context": "src"
                                            }
                                        ],
                                        "target": [
                                            {
                                                "context": "entry",
                                                "contextType": "variable",
                                                "element": "resource",
                                                "variable": "tgt",
                                                "transform": "create",
                                                "parameter": [
                                                    {
                                                        "valueString": "RelatedPerson"
                                                    }
                                                ]
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "ec9147dd",
                                                "source": [
                                                    {
                                                        "context": "src"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "contextType": "variable",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueId": "tgt"
                                                            }
                                                        ]
                                                    }
                                                ],
                                                "dependent": [
                                                    {
                                                        "name": "emcarerelatedperson",
                                                        "variable": [
                                                            "src",
                                                            "tgt"
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "name": "emcareobservation",
                                "source": [
                                    {
                                        "context": "src",
                                        "condition": "src.item.where(linkId = 'EmCare.B3.DE05').exists()"
                                    }
                                ],
                                "rule": [
                                    {
                                        "name": "act-EmCare.B3.DE05",
                                        "source": [
                                            {
                                                "context": "src"
                                            }
                                        ],
                                        "target": [
                                            {
                                                "context": "bundle",
                                                "contextType": "variable",
                                                "element": "entry",
                                                "variable": "entry"
                                            },
                                            {
                                                "context": "entry",
                                                "contextType": "variable",
                                                "element": "request",
                                                "variable": "request"
                                            },
                                            {
                                                "context": "request",
                                                "contextType": "variable",
                                                "element": "method",
                                                "transform": "copy",
                                                "parameter": [
                                                    {
                                                        "valueString": "POST"
                                                    }
                                                ]
                                            },
                                            {
                                                "context": "entry",
                                                "contextType": "variable",
                                                "element": "resource",
                                                "variable": "tgt",
                                                "transform": "create",
                                                "parameter": [
                                                    {
                                                        "valueString": "Observation"
                                                    }
                                                ]
                                            }
                                        ],
                                        "dependent": [
                                            {
                                                "name": "emcareobservationemcareb3de05",
                                                "variable": [
                                                    "src",
                                                    "tgt"
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "name": "emcareencounter",
                        "typeMode": "none",
                        "input": [
                            {
                                "name": "src",
                                "type": "questionnaireResponse",
                                "mode": "source"
                            },
                            {
                                "name": "tgt",
                                "type": "Encounter",
                                "mode": "target"
                            }
                        ],
                        "rule": [
                            {
                                "name": "emcareade07",
                                "source": [
                                    {
                                        "context": "src",
                                        "element": "item",
                                        "variable": "item",
                                        "condition": "linkId = 'EmCare.A.DE07'"
                                    }
                                ],
                                "rule": [
                                    {
                                        "name": "aemcareade07",
                                        "source": [
                                            {
                                                "context": "item",
                                                "element": "answer",
                                                "listMode": "first",
                                                "variable": "a"
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "aemcareade07",
                                                "source": [
                                                    {
                                                        "context": "a",
                                                        "element": "value",
                                                        "variable": "val"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "context": "tgt",
                                                        "contextType": "variable",
                                                        "element": "period",
                                                        "variable": "period"
                                                    },
                                                    {
                                                        "context": "period",
                                                        "contextType": "variable",
                                                        "element": "start",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueId": "val"
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "name": "emcareb2de01",
                                "source": [
                                    {
                                        "context": "src",
                                        "element": "item",
                                        "variable": "item",
                                        "condition": "linkId = 'EmCare.B2.DE01'"
                                    }
                                ],
                                "rule": [
                                    {
                                        "name": "aemcareb2de01",
                                        "source": [
                                            {
                                                "context": "item",
                                                "element": "answer",
                                                "listMode": "first",
                                                "variable": "a"
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "aemcareb2de01",
                                                "source": [
                                                    {
                                                        "context": "a",
                                                        "element": "value",
                                                        "variable": "val"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "context": "tgt",
                                                        "contextType": "variable",
                                                        "element": "type",
                                                        "variable": "CC",
                                                        "transform": "create",
                                                        "parameter": [
                                                            {
                                                                "valueString": "CodeableConcept"
                                                            }
                                                        ]
                                                    },
                                                    {
                                                        "context": "CC",
                                                        "contextType": "variable",
                                                        "element": "text",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueString": "type of visit"
                                                            }
                                                        ]
                                                    },
                                                    {
                                                        "context": "CC",
                                                        "contextType": "variable",
                                                        "element": "coding",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueId": "val"
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "name": "emcareb3de01",
                                "source": [
                                    {
                                        "context": "src",
                                        "element": "item",
                                        "variable": "item",
                                        "condition": "linkId = 'EmCare.B3.DE01'"
                                    }
                                ],
                                "rule": [
                                    {
                                        "name": "aemcareb3de01",
                                        "source": [
                                            {
                                                "context": "item",
                                                "element": "answer",
                                                "listMode": "first",
                                                "variable": "a"
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "aemcareb3de01",
                                                "source": [
                                                    {
                                                        "context": "a",
                                                        "element": "value",
                                                        "variable": "val"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "context": "tgt",
                                                        "contextType": "variable",
                                                        "element": "reasonCode",
                                                        "variable": "CC",
                                                        "transform": "create",
                                                        "parameter": [
                                                            {
                                                                "valueString": "CodeableConcept"
                                                            }
                                                        ]
                                                    },
                                                    {
                                                        "context": "CC",
                                                        "contextType": "variable",
                                                        "element": "text",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueString": "new consultation"
                                                            }
                                                        ]
                                                    },
                                                    {
                                                        "context": "CC",
                                                        "contextType": "variable",
                                                        "element": "coding",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueId": "val"
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "name": "emcareb3de06",
                                "source": [
                                    {
                                        "context": "src",
                                        "element": "item",
                                        "variable": "item",
                                        "condition": "linkId = 'EmCare.B3.DE06'"
                                    }
                                ],
                                "rule": [
                                    {
                                        "name": "aemcareb3de06",
                                        "source": [
                                            {
                                                "context": "item",
                                                "element": "answer",
                                                "listMode": "first",
                                                "variable": "a"
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "aemcareb3de06",
                                                "source": [
                                                    {
                                                        "context": "a",
                                                        "element": "value",
                                                        "variable": "val"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "context": "tgt",
                                                        "contextType": "variable",
                                                        "element": "type",
                                                        "variable": "CC",
                                                        "transform": "create",
                                                        "parameter": [
                                                            {
                                                                "valueString": "CodeableConcept"
                                                            }
                                                        ]
                                                    },
                                                    {
                                                        "context": "CC",
                                                        "contextType": "variable",
                                                        "element": "text",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueString": "new consultation"
                                                            }
                                                        ]
                                                    },
                                                    {
                                                        "context": "CC",
                                                        "contextType": "variable",
                                                        "element": "coding",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueId": "val"
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "name": "emcareb3de09",
                                "source": [
                                    {
                                        "context": "src",
                                        "element": "item",
                                        "variable": "item",
                                        "condition": "linkId = 'EmCare.B3.DE09'"
                                    }
                                ],
                                "rule": [
                                    {
                                        "name": "aemcareb3de09",
                                        "source": [
                                            {
                                                "context": "item",
                                                "element": "answer",
                                                "listMode": "first",
                                                "variable": "a"
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "aemcareb3de09",
                                                "source": [
                                                    {
                                                        "context": "a",
                                                        "element": "value",
                                                        "variable": "val"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "context": "tgt",
                                                        "contextType": "variable",
                                                        "element": "extension",
                                                        "variable": "ext",
                                                        "transform": "create",
                                                        "parameter": [
                                                            {
                                                                "valueString": "Extension"
                                                            }
                                                        ]
                                                    },
                                                    {
                                                        "context": "ext",
                                                        "contextType": "variable",
                                                        "element": "url",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueString": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/refered"
                                                            }
                                                        ]
                                                    },
                                                    {
                                                        "context": "ext",
                                                        "contextType": "variable",
                                                        "element": "value",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueId": "val"
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "name": "getIdemcareencounter",
                        "typeMode": "none",
                        "input": [
                            {
                                "name": "src",
                                "mode": "source"
                            },
                            {
                                "name": "tgt",
                                "mode": "target"
                            }
                        ],
                        "rule": [
                            {
                                "name": "269afe68",
                                "source": [
                                    {
                                        "context": "src",
                                        "element": "item",
                                        "listMode": "first",
                                        "variable": "item",
                                        "condition": "linkId = 'emcareencounterid'"
                                    }
                                ],
                                "target": [
                                    {
                                        "contextType": "variable",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueId": "tgt"
                                            }
                                        ]
                                    }
                                ],
                                "rule": [
                                    {
                                        "name": "f3550c0d",
                                        "source": [
                                            {
                                                "context": "item",
                                                "element": "answer",
                                                "listMode": "first",
                                                "variable": "a"
                                            }
                                        ],
                                        "target": [
                                            {
                                                "contextType": "variable",
                                                "transform": "copy",
                                                "parameter": [
                                                    {
                                                        "valueId": "tgt"
                                                    }
                                                ]
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "8e3eaf71",
                                                "source": [
                                                    {
                                                        "context": "a",
                                                        "element": "value",
                                                        "variable": "val"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "context": "tgt",
                                                        "contextType": "variable",
                                                        "element": "id",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueId": "val"
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "name": "getFullUrlemcareencounter",
                        "typeMode": "none",
                        "input": [
                            {
                                "name": "src",
                                "mode": "source"
                            },
                            {
                                "name": "tgt",
                                "mode": "target"
                            }
                        ],
                        "rule": [
                            {
                                "name": "d383794d",
                                "source": [
                                    {
                                        "context": "src",
                                        "element": "item",
                                        "listMode": "first",
                                        "variable": "item",
                                        "condition": "linkId = 'emcareencounterid'"
                                    }
                                ],
                                "target": [
                                    {
                                        "contextType": "variable",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueId": "tgt"
                                            }
                                        ]
                                    }
                                ],
                                "rule": [
                                    {
                                        "name": "8a0f6df2",
                                        "source": [
                                            {
                                                "context": "item",
                                                "element": "answer",
                                                "listMode": "first",
                                                "variable": "a"
                                            }
                                        ],
                                        "target": [
                                            {
                                                "contextType": "variable",
                                                "transform": "copy",
                                                "parameter": [
                                                    {
                                                        "valueId": "tgt"
                                                    }
                                                ]
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "a6735326",
                                                "source": [
                                                    {
                                                        "context": "a",
                                                        "element": "value",
                                                        "variable": "val"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "context": "tgt",
                                                        "contextType": "variable",
                                                        "element": "fullUrl",
                                                        "transform": "append",
                                                        "parameter": [
                                                            {
                                                                "valueString": "urn:uuid:"
                                                            },
                                                            {
                                                                "valueId": "val"
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "name": "getUrlemcareencounter",
                        "typeMode": "none",
                        "input": [
                            {
                                "name": "src",
                                "mode": "source"
                            },
                            {
                                "name": "tgt",
                                "mode": "target"
                            }
                        ],
                        "rule": [
                            {
                                "name": "558d60eb",
                                "source": [
                                    {
                                        "context": "src",
                                        "element": "item",
                                        "listMode": "first",
                                        "variable": "item",
                                        "condition": "linkId = 'emcareencounterid'"
                                    }
                                ],
                                "target": [
                                    {
                                        "contextType": "variable",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueId": "tgt"
                                            }
                                        ]
                                    }
                                ],
                                "rule": [
                                    {
                                        "name": "35ea1ad7",
                                        "source": [
                                            {
                                                "context": "item",
                                                "element": "answer",
                                                "listMode": "first",
                                                "variable": "a"
                                            }
                                        ],
                                        "target": [
                                            {
                                                "contextType": "variable",
                                                "transform": "copy",
                                                "parameter": [
                                                    {
                                                        "valueId": "tgt"
                                                    }
                                                ]
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "e8494603",
                                                "source": [
                                                    {
                                                        "context": "a",
                                                        "element": "value",
                                                        "variable": "val"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "context": "ref",
                                                        "contextType": "variable",
                                                        "element": "reference",
                                                        "transform": "append",
                                                        "parameter": [
                                                            {
                                                                "valueString": "/Encounter/"
                                                            },
                                                            {
                                                                "valueId": "val"
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "name": "emcarerelatedperson",
                        "typeMode": "none",
                        "input": [
                            {
                                "name": "src",
                                "type": "questionnaireResponse",
                                "mode": "source"
                            },
                            {
                                "name": "tgt",
                                "type": "RelatedPerson",
                                "mode": "target"
                            }
                        ],
                        "rule": [
                            {
                                "name": "emcareade40",
                                "source": [
                                    {
                                        "context": "src",
                                        "element": "item",
                                        "listMode": "first",
                                        "variable": "item",
                                        "condition": "(linkId = 'EmCare.A.DE40') or (linkId = 'EXXXXXXX') or (linkId = 'EmCare.A.DE40')"
                                    }
                                ],
                                "target": [
                                    {
                                        "contextType": "variable",
                                        "variable": "target",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueId": "tgt"
                                            }
                                        ]
                                    },
                                    {
                                        "context": "target",
                                        "contextType": "variable",
                                        "element": "name",
                                        "variable": "name"
                                    }
                                ],
                                "dependent": [
                                    {
                                        "name": "SetOfficalGivenNameemcarerelatedperson",
                                        "variable": [
                                            "src",
                                            "name"
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "name": "SetOfficalGivenNameemcarerelatedperson",
                        "typeMode": "none",
                        "input": [
                            {
                                "name": "src",
                                "mode": "source"
                            },
                            {
                                "name": "tgt",
                                "mode": "target"
                            }
                        ],
                        "rule": [
                            {
                                "name": "eb5b3f08",
                                "source": [
                                    {
                                        "context": "src"
                                    }
                                ],
                                "target": [
                                    {
                                        "context": "tgt",
                                        "contextType": "variable",
                                        "element": "use",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueString": "official"
                                            }
                                        ]
                                    }
                                ],
                                "rule": [
                                    {
                                        "name": "3674e72e",
                                        "source": [
                                            {
                                                "context": "src",
                                                "element": "item",
                                                "variable": "item",
                                                "condition": "linkId = 'EmCare.A.DE40'"
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "dcd99d74",
                                                "source": [
                                                    {
                                                        "context": "item",
                                                        "element": "answer",
                                                        "listMode": "first",
                                                        "variable": "a"
                                                    }
                                                ],
                                                "rule": [
                                                    {
                                                        "name": "577eab5d",
                                                        "source": [
                                                            {
                                                                "context": "a",
                                                                "element": "value",
                                                                "variable": "val"
                                                            }
                                                        ],
                                                        "target": [
                                                            {
                                                                "context": "tgt",
                                                                "contextType": "variable",
                                                                "element": "given",
                                                                "transform": "copy",
                                                                "parameter": [
                                                                    {
                                                                        "valueId": "val"
                                                                    }
                                                                ]
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    },
                                    {
                                        "name": "901b64d6",
                                        "source": [
                                            {
                                                "context": "src",
                                                "element": "item",
                                                "variable": "item",
                                                "condition": "linkId = 'EXXXXXXX'"
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "dcd99d74",
                                                "source": [
                                                    {
                                                        "context": "item",
                                                        "element": "answer",
                                                        "listMode": "first",
                                                        "variable": "a"
                                                    }
                                                ],
                                                "rule": [
                                                    {
                                                        "name": "577eab5d",
                                                        "source": [
                                                            {
                                                                "context": "a",
                                                                "element": "value",
                                                                "variable": "val"
                                                            }
                                                        ],
                                                        "target": [
                                                            {
                                                                "context": "tgt",
                                                                "contextType": "variable",
                                                                "element": "given",
                                                                "transform": "copy",
                                                                "parameter": [
                                                                    {
                                                                        "valueId": "val"
                                                                    }
                                                                ]
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    },
                                    {
                                        "name": "512ccb78",
                                        "source": [
                                            {
                                                "context": "src",
                                                "element": "item",
                                                "variable": "item",
                                                "condition": "linkId = 'EmCare.A.DE40'"
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "09801739",
                                                "source": [
                                                    {
                                                        "context": "item",
                                                        "element": "answer",
                                                        "listMode": "first",
                                                        "variable": "a"
                                                    }
                                                ],
                                                "rule": [
                                                    {
                                                        "name": "2374a9a1",
                                                        "source": [
                                                            {
                                                                "context": "a",
                                                                "element": "value",
                                                                "variable": "val"
                                                            }
                                                        ],
                                                        "target": [
                                                            {
                                                                "context": "tgt",
                                                                "contextType": "variable",
                                                                "element": "family",
                                                                "transform": "copy",
                                                                "parameter": [
                                                                    {
                                                                        "valueId": "val"
                                                                    }
                                                                ]
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "name": "getIdemcarerelatedperson",
                        "typeMode": "none",
                        "input": [
                            {
                                "name": "src",
                                "mode": "source"
                            },
                            {
                                "name": "tgt",
                                "mode": "target"
                            }
                        ],
                        "rule": [
                            {
                                "name": "de9cc039",
                                "source": [
                                    {
                                        "context": "src",
                                        "element": "item",
                                        "listMode": "first",
                                        "variable": "item",
                                        "condition": "linkId = 'emcarerelatedpersonid'"
                                    }
                                ],
                                "target": [
                                    {
                                        "contextType": "variable",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueId": "tgt"
                                            }
                                        ]
                                    }
                                ],
                                "rule": [
                                    {
                                        "name": "f3550c0d",
                                        "source": [
                                            {
                                                "context": "item",
                                                "element": "answer",
                                                "listMode": "first",
                                                "variable": "a"
                                            }
                                        ],
                                        "target": [
                                            {
                                                "contextType": "variable",
                                                "transform": "copy",
                                                "parameter": [
                                                    {
                                                        "valueId": "tgt"
                                                    }
                                                ]
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "8e3eaf71",
                                                "source": [
                                                    {
                                                        "context": "a",
                                                        "element": "value",
                                                        "variable": "val"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "context": "tgt",
                                                        "contextType": "variable",
                                                        "element": "id",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueId": "val"
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "name": "getFullUrlemcarerelatedperson",
                        "typeMode": "none",
                        "input": [
                            {
                                "name": "src",
                                "mode": "source"
                            },
                            {
                                "name": "tgt",
                                "mode": "target"
                            }
                        ],
                        "rule": [
                            {
                                "name": "00c7b89d",
                                "source": [
                                    {
                                        "context": "src",
                                        "element": "item",
                                        "listMode": "first",
                                        "variable": "item",
                                        "condition": "linkId = 'emcarerelatedpersonid'"
                                    }
                                ],
                                "target": [
                                    {
                                        "contextType": "variable",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueId": "tgt"
                                            }
                                        ]
                                    }
                                ],
                                "rule": [
                                    {
                                        "name": "8a0f6df2",
                                        "source": [
                                            {
                                                "context": "item",
                                                "element": "answer",
                                                "listMode": "first",
                                                "variable": "a"
                                            }
                                        ],
                                        "target": [
                                            {
                                                "contextType": "variable",
                                                "transform": "copy",
                                                "parameter": [
                                                    {
                                                        "valueId": "tgt"
                                                    }
                                                ]
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "a6735326",
                                                "source": [
                                                    {
                                                        "context": "a",
                                                        "element": "value",
                                                        "variable": "val"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "context": "tgt",
                                                        "contextType": "variable",
                                                        "element": "fullUrl",
                                                        "transform": "append",
                                                        "parameter": [
                                                            {
                                                                "valueString": "urn:uuid:"
                                                            },
                                                            {
                                                                "valueId": "val"
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "name": "getUrlemcarerelatedperson",
                        "typeMode": "none",
                        "input": [
                            {
                                "name": "src",
                                "mode": "source"
                            },
                            {
                                "name": "tgt",
                                "mode": "target"
                            }
                        ],
                        "rule": [
                            {
                                "name": "ce147194",
                                "source": [
                                    {
                                        "context": "src",
                                        "element": "item",
                                        "listMode": "first",
                                        "variable": "item",
                                        "condition": "linkId = 'emcarerelatedpersonid'"
                                    }
                                ],
                                "target": [
                                    {
                                        "contextType": "variable",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueId": "tgt"
                                            }
                                        ]
                                    }
                                ],
                                "rule": [
                                    {
                                        "name": "00b2016a",
                                        "source": [
                                            {
                                                "context": "item",
                                                "element": "answer",
                                                "listMode": "first",
                                                "variable": "a"
                                            }
                                        ],
                                        "target": [
                                            {
                                                "contextType": "variable",
                                                "transform": "copy",
                                                "parameter": [
                                                    {
                                                        "valueId": "tgt"
                                                    }
                                                ]
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "25b16148",
                                                "source": [
                                                    {
                                                        "context": "a",
                                                        "element": "value",
                                                        "variable": "val"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "context": "ref",
                                                        "contextType": "variable",
                                                        "element": "reference",
                                                        "transform": "append",
                                                        "parameter": [
                                                            {
                                                                "valueString": "/RelatedPerson/"
                                                            },
                                                            {
                                                                "valueId": "val"
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "name": "emcareobservationemcareb3de05",
                        "typeMode": "none",
                        "input": [
                            {
                                "name": "src",
                                "mode": "source"
                            },
                            {
                                "name": "tgt",
                                "mode": "target"
                            }
                        ],
                        "rule": [
                            {
                                "name": "id-emcareb3de05",
                                "source": [
                                    {
                                        "context": "src"
                                    }
                                ],
                                "target": [
                                    {
                                        "context": "tgt",
                                        "contextType": "variable",
                                        "element": "identifier",
                                        "variable": "CodeID",
                                        "transform": "create",
                                        "parameter": [
                                            {
                                                "valueString": "Identifier"
                                            }
                                        ]
                                    },
                                    {
                                        "context": "CodeID",
                                        "contextType": "variable",
                                        "element": "system",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueString": "http://hl7.org/fhir/namingsystem-identifier-type"
                                            }
                                        ]
                                    },
                                    {
                                        "context": "CodeID",
                                        "contextType": "variable",
                                        "element": "use",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueString": "official"
                                            }
                                        ]
                                    },
                                    {
                                        "context": "CodeID",
                                        "contextType": "variable",
                                        "element": "value",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueString": "uuid"
                                            }
                                        ]
                                    },
                                    {
                                        "context": "CodeID",
                                        "contextType": "variable",
                                        "element": "id",
                                        "transform": "uuid"
                                    }
                                ]
                            },
                            {
                                "name": "code-emcareb3de05",
                                "source": [
                                    {
                                        "context": "src"
                                    }
                                ],
                                "target": [
                                    {
                                        "context": "tgt",
                                        "contextType": "variable",
                                        "element": "encounter",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueId": "src.encounter"
                                            }
                                        ]
                                    },
                                    {
                                        "context": "tgt",
                                        "contextType": "variable",
                                        "element": "subject",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueId": "src.subject"
                                            }
                                        ]
                                    },
                                    {
                                        "context": "tgt",
                                        "contextType": "variable",
                                        "element": "meta",
                                        "variable": "newMeta",
                                        "transform": "create",
                                        "parameter": [
                                            {
                                                "valueString": "Meta"
                                            }
                                        ]
                                    },
                                    {
                                        "context": "newMeta",
                                        "contextType": "variable",
                                        "element": "profile",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueString": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/StructureDefinition/emcareobservation"
                                            }
                                        ]
                                    },
                                    {
                                        "context": "tgt",
                                        "contextType": "variable",
                                        "element": "code",
                                        "variable": "concept",
                                        "transform": "create",
                                        "parameter": [
                                            {
                                                "valueString": "CodeableConcept"
                                            }
                                        ]
                                    },
                                    {
                                        "context": "concept",
                                        "contextType": "variable",
                                        "element": "system",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueString": "https://fhir.dk.swisstph-mis.ch/matchbox/fhir/CodeSystem/emcare-custom-codes"
                                            }
                                        ]
                                    },
                                    {
                                        "context": "concept",
                                        "contextType": "variable",
                                        "element": "code",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueString": "EmCare.B3.DE05"
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "name": "timestamp-emcareb3de05",
                                "source": [
                                    {
                                        "context": "src",
                                        "element": "item",
                                        "variable": "item",
                                        "condition": "linkId = 'timestamp'"
                                    },
                                    {
                                        "context": "item",
                                        "element": "answer",
                                        "variable": "a"
                                    }
                                ],
                                "target": [
                                    {
                                        "context": "tgt",
                                        "contextType": "variable",
                                        "element": "issued",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueId": "a"
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "name": "patient",
                                "source": [
                                    {
                                        "context": "src"
                                    }
                                ],
                                "target": [
                                    {
                                        "context": "tgt",
                                        "contextType": "variable",
                                        "element": "subject",
                                        "transform": "copy",
                                        "parameter": [
                                            {
                                                "valueId": "src.subject"
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "name": "it-emcareb3de05",
                                "source": [
                                    {
                                        "context": "src",
                                        "element": "item",
                                        "listMode": "first",
                                        "variable": "item",
                                        "condition": "linkId = 'EmCare.B3.DE05'"
                                    }
                                ],
                                "rule": [
                                    {
                                        "name": "an-emcareb3de05",
                                        "source": [
                                            {
                                                "context": "item",
                                                "element": "answer",
                                                "listMode": "first",
                                                "variable": "a"
                                            }
                                        ],
                                        "rule": [
                                            {
                                                "name": "final-emcareb3de05",
                                                "source": [
                                                    {
                                                        "context": "a",
                                                        "condition": "a.value = 'yes'"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "context": "tgt",
                                                        "contextType": "variable",
                                                        "element": "status",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueString": "final"
                                                            }
                                                        ]
                                                    }
                                                ]
                                            },
                                            {
                                                "name": "notfound-emcareb3de05",
                                                "source": [
                                                    {
                                                        "context": "a",
                                                        "condition": "a.value = 'no'"
                                                    }
                                                ],
                                                "target": [
                                                    {
                                                        "context": "tgt",
                                                        "contextType": "variable",
                                                        "element": "status",
                                                        "transform": "copy",
                                                        "parameter": [
                                                            {
                                                                "valueString": "cancelled"
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }"""
      val structureMap = FhirContext.forR4().newJsonParser().parseResource(structureMapString) as StructureMap
      val entry = ResourceMapper.extract(
        questionnaireResource,
        questionnaireResponse,
        StructureMapExtractionContext(context = getApplication()) { _, _ -> structureMap
        }
      )
      print("The Extracted Bundle:")
      print(FhirContext.forR4().newJsonParser().encodeResourceToString(entry))
    }
  }

  private fun getQuestionnaireJson(): String {
//    questionnaireJson?.let {
//      return it!!
//    }
    val parser = FhirContext.forR4().newJsonParser()
    questionnaireJson = readFileFromAssets(state[AddPatientFragment.QUESTIONNAIRE_FILE_PATH_KEY]!!)
    val questionnaireJsonObject = injectUuid(parser.parseResource(Questionnaire::class.java, questionnaireJson))
    questionnaireJson = parser.encodeResourceToString(questionnaireJsonObject)
    questionnaireResponse = parser.encodeResourceToString(generateQuestionnaireResponseWithPatientIdAndEncounterId(questionnaireJsonObject, UUID.randomUUID().toString(), UUID.randomUUID().toString()))
    return questionnaireJson!!
  }
  private fun injectUuid(questionnaire: Questionnaire) : Questionnaire {
    questionnaire.item.forEach { item ->
      if(!item.initial.isNullOrEmpty()) {
        if(item.initial[0].value.asStringValue() == "uuid()") {
          item.initial =
            mutableListOf(Questionnaire.QuestionnaireItemInitialComponent(StringType(
              UUID.randomUUID().toString())))
        }
      }
    }
    return questionnaire
  }


  private fun generateQuestionnaireResponseWithPatientIdAndEncounterId(questionnaireJson: Questionnaire, patientId: String, encounterId: String) : QuestionnaireResponse {
    //Create empty QR as done in the SDC
    val questionnaireResponse:QuestionnaireResponse = QuestionnaireResponse().apply {
      questionnaire = questionnaireJson.url
    }
    questionnaireJson.item.forEach { it2 ->
      questionnaireResponse.addItem(it2.createQuestionnaireResponseItem())
    }

    //Inject patientId as subject & encounterId as Encounter.
    questionnaireResponse.subject = Reference().apply {
      id = IdType(patientId).id
      type = ResourceType.Patient.name
      identifier = Identifier().apply {
        value = patientId
      }
    }
    questionnaireResponse.encounter = Reference().apply {
      id = encounterId
      type = ResourceType.Encounter.name
      identifier = Identifier().apply {
        value = encounterId
      }
    }

    return questionnaireResponse
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
